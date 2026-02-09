# Frontend Development Workflow

## 🌿 Branching Strategy

### **Main Branches**
- `main` - Production-ready frontend code
- `frontend-dev` - Integration branch for frontend features
- `develop` - Overall project integration (includes backend)

### **Feature Branch Workflow**
```bash
# Start new frontend feature
git checkout frontend-dev
git pull origin frontend-dev
git checkout -b feature/frontend-auth-ui

# Work on your feature...
# Commit changes
git add .
git commit -m "feat: add authentication UI components"

# Push and create PR
git push origin feature/frontend-auth-ui
# Create PR: feature/frontend-auth-ui -> frontend-dev
```

### **Bug Fix Workflow**
```bash
# For frontend bugs
git checkout frontend-dev
git pull origin frontend-dev
git checkout -b bugfix/frontend-login-validation-error

# Fix the issue...
git add .
git commit -m "fix: resolve login form validation error"

git push origin bugfix/frontend-login-validation-error
# Create PR: bugfix/frontend-login-validation-error -> frontend-dev
```

## 🐛 Frontend Issue Handling

### **1. Identify the Issue Type**
- **UI Bug**: Visual/layout issues
- **Logic Bug**: Component behavior problems
- **Performance**: Slow loading/rendering
- **Integration**: API communication issues

### **2. Debugging Workflow**

#### **Local Development Setup**
```bash
# Start frontend with backend services
docker-compose up postgres redis kafka -d
cd frontend
npm install
npm run dev
```

#### **Browser DevTools Debugging**
1. **Console Errors**: Check for JavaScript errors
2. **Network Tab**: Verify API calls and responses
3. **Elements Tab**: Inspect HTML/CSS issues
4. **Performance Tab**: Analyze rendering performance

#### **React DevTools**
```bash
# Install React DevTools browser extension
# Component state inspection
# Props debugging
# Performance profiling
```

### **3. Common Frontend Issues & Solutions**

#### **API Connection Issues**
```typescript
// Check API configuration in frontend/src/lib/api.ts
const API_BASE_URL = process.env.VITE_API_URL || 'http://localhost:8080';

// Verify CORS settings in backend gateway
// Check network requests in browser dev tools
```

#### **State Management Issues**
```typescript
// Debug Zustand store state
import { useAuthStore } from '@/store/auth';

// Add console.log for state changes
console.log('Auth state:', useAuthStore.getState());

// Check state persistence
localStorage.getItem('auth-storage');
```

#### **Routing Issues**
```typescript
// Check React Router configuration
// Verify protected routes
// Debug navigation history
```

### **4. Testing Strategy**

#### **Unit Tests**
```bash
# Run specific component tests
npm test -- --testPathPattern=LoginForm
npm test -- --testPathPattern=ProjectList

# Run tests in watch mode
npm test -- --watch
```

#### **Integration Tests**
```bash
# Test API integration
npm run test:integration

# Test user flows
npm run test:e2e
```

#### **Visual Regression Testing**
```bash
# Run visual tests
npm run test:visual
```

## 🔄 CI/CD Pipeline for Frontend

### **Automated Checks**
- **ESLint**: Code quality and style
- **Prettier**: Code formatting
- **TypeScript**: Type checking
- **Unit Tests**: Functionality verification
- **Build**: Production build verification
- **Lighthouse**: Performance and accessibility

### **Deployment Pipeline**
1. **Push to frontend-dev** → Runs tests → Deploys to staging
2. **PR to main** → Full test suite → Code review
3. **Merge to main** → Production deployment

## 🛠️ Development Tools

### **VS Code Extensions**
- ES7+ React/Redux/React-Native snippets
- TypeScript Importer
- Prettier - Code formatter
- ESLint
- Auto Rename Tag
- Bracket Pair Colorizer
- GitLens

### **Browser Extensions**
- React Developer Tools
- Redux DevTools (if using Redux)
- Lighthouse
- WhatFont
- ColorZilla

### **Helpful NPM Scripts**
```json
{
  "scripts": {
    "dev": "vite",
    "build": "tsc && vite build",
    "preview": "vite preview",
    "test": "vitest",
    "test:ui": "vitest --ui",
    "test:coverage": "vitest --coverage",
    "lint": "eslint . --ext ts,tsx --report-unused-disable-directives --max-warnings 0",
    "lint:fix": "eslint . --ext ts,tsx --fix",
    "format": "prettier --write .",
    "format:check": "prettier --check .",
    "type-check": "tsc --noEmit"
  }
}
```

## 📊 Performance Monitoring

### **Local Performance Testing**
```bash
# Bundle analysis
npm run build -- --analyze

# Lighthouse CI
npm run lhci

# Performance profiling
npm run dev -- --profile
```

### **Production Monitoring**
- **Core Web Vitals**: LCP, FID, CLS
- **Bundle Size**: Monitor JavaScript bundle size
- **API Response Times**: Track backend communication
- **Error Tracking**: Sentry or similar error monitoring

## 🚀 Deployment Best Practices

### **Environment Variables**
```bash
# .env.staging
VITE_API_URL=https://staging-api.yoursaas.com
VITE_WS_URL=wss://staging-ws.yoursaas.com
VITE_ENV=staging

# .env.production
VITE_API_URL=https://api.yoursaas.com
VITE_WS_URL=wss://ws.yoursaas.com
VITE_ENV=production
```

### **Build Optimization**
- **Code Splitting**: Lazy load components
- **Tree Shaking**: Remove unused code
- **Asset Optimization**: Compress images and fonts
- **Caching**: Implement proper cache headers

## 🤝 Collaboration Guidelines

### **Code Review Checklist**
- [ ] Functionality works as expected
- [ ] No console errors or warnings
- [ ] Responsive design works
- [ ] Accessibility standards met
- [ ] Performance impact considered
- [ ] Tests added/updated
- [ ] Documentation updated

### **Commit Message Format**
```
type(scope): description

feat(auth): add login form validation
fix(ui): resolve responsive layout issue on mobile
docs(readme): update setup instructions
style(components): improve button component styling
refactor(hooks): optimize useAuth hook performance
test(components): add unit tests for ProjectCard
```

This workflow ensures systematic frontend development with proper issue handling and quality assurance.
