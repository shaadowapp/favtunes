# Firebase Security Rules for Bug Report System

This directory contains Firebase Firestore security rules and tests for the bug report and feedback system.

## Security Rules Overview

The security rules implement the following access patterns:

### Bug Reports Collection (`/bug_reports/{document}`)

- **Create**: Allowed for both authenticated and anonymous users with valid data
- **Read**: Only authenticated users can read their own reports
- **Update**: Only status field updates allowed by report owners
- **Delete**: Only report owners can delete their reports

### User Feedback Collection (`/user_feedback/{document}`)

- **Create**: Allowed for both authenticated and anonymous users with valid data
- **Read**: Only authenticated users can read their own non-anonymous feedback
- **Update**: Not allowed (feedback is immutable)
- **Delete**: Only owners can delete their non-anonymous feedback

### Legacy Feedback Collection (`/feedbacks/{document}`)

- **Create**: Allowed for backward compatibility
- **Read/Update/Delete**: Not allowed

## Validation Rules

### Bug Report Validation

- Title: 3-200 characters
- Description: 10-5000 characters
- Severity: Must be one of `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`
- Category: Must be one of `UI`, `PLAYBACK`, `SYNC`, `PERFORMANCE`, `CRASH`, `OTHER`
- App Version: Must follow semantic versioning (e.g., "2.2.1")
- Timestamp: Must be within the last hour (prevents replay attacks)
- Device Info: Must contain all required fields

### Feedback Validation

- Rating: Must be between 1 and 5
- Category: Must be one of `GENERAL`, `FEATURE_REQUEST`, `UI_UX`, `PERFORMANCE`, `OTHER`
- Message: 5-2000 characters
- App Version: Must follow semantic versioning
- Timestamp: Must be within the last hour

## Security Features

1. **Anonymous Submissions**: Supported with proper validation
2. **Replay Attack Prevention**: Timestamp validation ensures recent submissions
3. **Data Validation**: Comprehensive validation of all fields
4. **Access Control**: Users can only access their own data
5. **Immutable Feedback**: Feedback cannot be modified once submitted
6. **Rate Limiting**: Placeholder for rate limiting implementation

## Testing

### Prerequisites

```bash
npm install
```

### Running Tests

```bash
# Run all tests
npm test

# Run tests in watch mode
npm run test:watch

# Run tests with coverage
npm run test:coverage
```

### Test Coverage

The tests cover:

- Valid data submissions (authenticated and anonymous)
- Invalid data rejection
- Access control enforcement
- Security edge cases
- Legacy compatibility

## Deployment

To deploy the security rules to Firebase:

```bash
firebase deploy --only firestore:rules
```

## Security Considerations

1. **Rate Limiting**: The current rules include a placeholder for rate limiting. In production, implement proper rate limiting using Firebase Functions or external services.

2. **Admin Access**: Admin access is controlled via custom claims. Ensure proper admin user management.

3. **Data Privacy**: Anonymous submissions are properly handled to prevent data leakage.

4. **Validation**: All user input is validated both client-side and server-side through security rules.

5. **Audit Trail**: Consider implementing audit logging for sensitive operations.

## Monitoring

Monitor the following metrics:

- Failed rule evaluations
- Unusual access patterns
- Rate limit violations
- Anonymous vs authenticated submission ratios

## Troubleshooting

### Common Issues

1. **Permission Denied**: Check that the user is authenticated and accessing their own data
2. **Validation Errors**: Ensure all required fields are present and valid
3. **Timestamp Errors**: Ensure timestamps are recent (within 1 hour)
4. **App Version Errors**: Ensure app version follows semantic versioning

### Debug Mode

Enable debug mode in Firebase console to see detailed rule evaluation logs.