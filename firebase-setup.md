# Firebase Setup for Bug Report and Feedback System

## Overview
This document provides instructions for setting up Firebase Firestore with security rules for the bug report and feedback system.

## Prerequisites
- Firebase project created
- Firebase CLI installed (`npm install -g firebase-tools`)
- Firebase project initialized in your app

## Firestore Security Rules

### 1. Deploy Security Rules
The security rules are defined in `firestore.rules`. To deploy them:

```bash
firebase deploy --only firestore:rules
```

### 2. Collections Structure

#### Bug Reports Collection (`bug_reports`)
- **Path**: `/bug_reports/{documentId}`
- **Fields**:
  - `id`: String (auto-generated)
  - `userId`: String (optional for anonymous)
  - `title`: String (1-200 chars)
  - `description`: String (1-5000 chars)
  - `severity`: Enum ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL']
  - `category`: Enum ['UI', 'PLAYBACK', 'SYNC', 'PERFORMANCE', 'CRASH', 'OTHER']
  - `deviceInfo`: Map
  - `appVersion`: String
  - `timestamp`: Timestamp
  - `attachments`: Array (optional)
  - `status`: Enum (default: 'OPEN')
  - `reproductionSteps`: Array (optional)

#### User Feedback Collection (`user_feedback`)
- **Path**: `/user_feedback/{documentId}`
- **Fields**:
  - `id`: String (auto-generated)
  - `userId`: String (optional for anonymous)
  - `rating`: Number (1-5)
  - `category`: Enum ['GENERAL', 'FEATURE_REQUEST', 'UI_UX', 'PERFORMANCE', 'OTHER']
  - `message`: String (1-2000 chars)
  - `deviceInfo`: Map
  - `appVersion`: String
  - `timestamp`: Timestamp
  - `isAnonymous`: Boolean

### 3. Security Rules Features

#### Access Control
- **Anonymous Submissions**: Allowed with proper validation
- **Authenticated Users**: Can create, read, update, delete their own data
- **Admin Users**: Can read all submissions (requires admin role setup)

#### Data Validation
- **Field Requirements**: All required fields must be present
- **Data Types**: Strict type checking for all fields
- **Size Limits**: Text fields have character limits
- **Enum Validation**: Category and severity fields validated against allowed values

#### Rate Limiting
- Basic rate limiting prevents rapid successive submissions
- More sophisticated rate limiting should be implemented server-side

### 4. Testing Security Rules

#### Setup Testing Environment
```bash
cd firebase-test
npm install
```

#### Run Tests
```bash
npm test
```

#### Test Coverage
- Anonymous and authenticated user access
- Data validation for all fields
- Access control between users
- Admin access permissions
- Invalid data rejection

### 5. Admin Setup (Optional)

To enable admin access for reading all submissions:

1. Create an admin user document:
```javascript
// In Firestore console or via admin SDK
db.collection('admin_users').doc('admin-user-id').set({
  role: 'admin',
  email: 'admin@example.com',
  createdAt: firebase.firestore.FieldValue.serverTimestamp()
});
```

2. Admin users can then read all bug reports and feedback

### 6. Monitoring and Analytics

#### Firestore Usage
- Monitor document reads/writes in Firebase console
- Set up billing alerts for usage limits
- Track collection sizes and growth

#### Security Monitoring
- Monitor failed security rule attempts
- Set up alerts for suspicious activity
- Regular security rule audits

### 7. Best Practices

#### Security
- Regularly review and update security rules
- Monitor for abuse and implement additional rate limiting if needed
- Consider implementing server-side validation for critical operations

#### Performance
- Use composite indexes for complex queries
- Implement pagination for large result sets
- Cache frequently accessed data

#### Cost Optimization
- Implement data retention policies
- Archive old submissions to reduce storage costs
- Optimize query patterns to minimize reads

### 8. Troubleshooting

#### Common Issues
1. **Permission Denied**: Check security rules and user authentication
2. **Invalid Data**: Verify all required fields and data types
3. **Rate Limiting**: Implement proper delays between submissions

#### Debug Tools
- Firebase Emulator Suite for local testing
- Firestore Rules Playground for rule testing
- Firebase Console for monitoring and debugging

### 9. Production Deployment

#### Pre-deployment Checklist
- [ ] Security rules tested with emulator
- [ ] All required indexes created
- [ ] Monitoring and alerts configured
- [ ] Backup strategy implemented
- [ ] Rate limiting configured

#### Deployment Steps
1. Test rules in emulator environment
2. Deploy to staging environment
3. Run integration tests
4. Deploy to production
5. Monitor for issues

### 10. Maintenance

#### Regular Tasks
- Review security logs monthly
- Update security rules as needed
- Monitor storage usage and costs
- Archive old data according to retention policy
- Update documentation as system evolves