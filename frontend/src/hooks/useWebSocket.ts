import { useEffect, useRef, useState } from 'react';
import { useAuthStore } from '@/store/auth';

interface WebSocketMessage {
  type: 'NOTIFICATION' | 'PROJECT_UPDATE' | 'TASK_UPDATE';
  data: any;
  timestamp: number;
}

interface Notification {
  id: string;
  type: 'INFO' | 'WARNING' | 'ERROR' | 'SUCCESS';
  title: string;
  message: string;
  timestamp: number;
  read: boolean;
}

export const useWebSocket = () => {
  const [isConnected, setIsConnected] = useState(false);
  const [notifications, setNotifications] = useState<Notification[]>([]);
  const [error, setError] = useState<string | null>(null);
  const wsRef = useRef<WebSocket | null>(null);
  const { isAuthenticated, accessToken, tenantId } = useAuthStore();
  const reconnectTimeoutRef = useRef<number | null>(null);

  const connect = () => {
    if (!isAuthenticated || !accessToken || !tenantId) {
      return;
    }

    const wsUrl = `${import.meta.env.VITE_WS_URL || 'ws://localhost:8080'}/ws?token=${accessToken}&tenantId=${tenantId}`;
    
    try {
      wsRef.current = new WebSocket(wsUrl);

      wsRef.current.onopen = () => {
        setIsConnected(true);
        setError(null);
        console.log('WebSocket connected');
      };

      wsRef.current.onmessage = (event) => {
        try {
          const message: WebSocketMessage = JSON.parse(event.data);
          
          switch (message.type) {
            case 'NOTIFICATION':
              const notification: Notification = {
                id: `notif-${Date.now()}-${Math.random()}`,
                ...message.data,
                read: false,
              };
              setNotifications(prev => [notification, ...prev]);
              break;
            
            case 'PROJECT_UPDATE':
              // Handle project updates
              console.log('Project update received:', message.data);
              break;
            
            case 'TASK_UPDATE':
              // Handle task updates
              console.log('Task update received:', message.data);
              break;
            
            default:
              console.log('Unknown message type:', message.type);
          }
        } catch (err) {
          console.error('Error parsing WebSocket message:', err);
        }
      };

      wsRef.current.onclose = (event) => {
        setIsConnected(false);
        console.log('WebSocket disconnected:', event.code, event.reason);
        
        // Attempt to reconnect after 5 seconds
        if (event.code !== 1000) { // Not a normal closure
          reconnectTimeoutRef.current = setTimeout(() => {
            connect();
          }, 5000);
        }
      };

      wsRef.current.onerror = (error) => {
        setError('WebSocket connection error');
        console.error('WebSocket error:', error);
      };
    } catch (err) {
      setError('Failed to connect to WebSocket');
      console.error('WebSocket connection failed:', err);
    }
  };

  const disconnect = () => {
    if (reconnectTimeoutRef.current) {
      clearTimeout(reconnectTimeoutRef.current);
      reconnectTimeoutRef.current = null;
    }
    
    if (wsRef.current) {
      wsRef.current.close(1000, 'User disconnected');
      wsRef.current = null;
    }
    
    setIsConnected(false);
  };

  const markAsRead = (notificationId: string) => {
    setNotifications(prev => 
      prev.map(notif => 
        notif.id === notificationId ? { ...notif, read: true } : notif
      )
    );
  };

  const markAllAsRead = () => {
    setNotifications(prev => 
      prev.map(notif => ({ ...notif, read: true }))
    );
  };

  const clearNotifications = () => {
    setNotifications([]);
  };

  const unreadCount = notifications.filter(n => !n.read).length;

  useEffect(() => {
    if (isAuthenticated && accessToken && tenantId) {
      connect();
    } else {
      disconnect();
    }

    return () => {
      disconnect();
    };
  }, [isAuthenticated, accessToken, tenantId]);

  return {
    isConnected,
    notifications,
    unreadCount,
    error,
    markAsRead,
    markAllAsRead,
    clearNotifications,
    reconnect: connect,
  };
};
