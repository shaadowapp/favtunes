const firebase = require('@firebase/rules-unit-testing');
const fs = require('fs');

const PROJECT_ID = 'tunes-test';
const RULES_FILE = 'firestore.rules';

// Test data
const validBugReport = {
  id: 'test-bug-report-1',
  title: 'Test Bug Report',
  description: 'This is a detailed description of the test bug that is long enough to pass validation',
  severity: 'MEDIUM',
  category: 'UI',
  deviceInfo: {
    deviceModel: 'Test Device',
    osVersion: 'Android 13',
    appVersion: '2.2.1',
    locale: 'en-US',
    screenResolution: '1080x2400',
    availableMemory: 8589934592,
    networkType: 'WiFi'
  },
  appVersion: '2.2.1',
  timestamp: Date.now(),
  attachments: [],
  status: 'OPEN',
  reproductionSteps: ['Step 1', 'Step 2'],
  userId: null
};

const validFeedback = {
  id: 'test-feedback-1',
  rating: 4,
  category: 'GENERAL',
  message: 'This is a test feedback message that is long enough',
  deviceInfo: {
    deviceModel: 'Test Device',
    osVersion: 'Android 13',
    appVersion: '2.2.1',
    locale: 'en-US',
    screenResolution: '1080x2400',
    availableMemory: 8589934592,
    networkType: 'WiFi'
  },
  appVersion: '2.2.1',
  timestamp: Date.now(),
  isAnonymous: true,
  userId: null
};

const authenticatedUser = {
  uid: 'test-user-1',
  email: 'test@example.com'
};

describe('Firestore Security Rules', () => {
  let testEnv;

  beforeAll(async () => {
    testEnv = await firebase.initializeTestEnvironment({
      projectId: PROJECT_ID,
      firestore: {
        rules: fs.readFileSync(RULES_FILE, 'utf8'),
      },
    });
  });

  afterAll(async () => {
    await testEnv.cleanup();
  });

  beforeEach(async () => {
    await testEnv.clearFirestore();
  });

  describe('Bug Reports Collection', () => {
    test('should allow anonymous bug report creation with valid data', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const bugReportRef = db.collection('bug_reports').doc(validBugReport.id);
      
      await firebase.assertSucceeds(bugReportRef.set(validBugReport));
    });

    test('should allow authenticated bug report creation', async () => {
      const db = testEnv.authenticatedContext(authenticatedUser.uid).firestore();
      const bugReportWithUser = {
        ...validBugReport,
        userId: authenticatedUser.uid,
        id: 'test-bug-report-auth'
      };
      const bugReportRef = db.collection('bug_reports').doc(bugReportWithUser.id);
      
      await firebase.assertSucceeds(bugReportRef.set(bugReportWithUser));
    });

    test('should reject bug report with invalid title', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const invalidBugReport = {
        ...validBugReport,
        title: 'Hi', // Too short
        id: 'test-invalid-title'
      };
      const bugReportRef = db.collection('bug_reports').doc(invalidBugReport.id);
      
      await firebase.assertFails(bugReportRef.set(invalidBugReport));
    });

    test('should reject bug report with invalid description', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const invalidBugReport = {
        ...validBugReport,
        description: 'Short', // Too short
        id: 'test-invalid-desc'
      };
      const bugReportRef = db.collection('bug_reports').doc(invalidBugReport.id);
      
      await firebase.assertFails(bugReportRef.set(invalidBugReport));
    });

    test('should reject bug report with invalid severity', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const invalidBugReport = {
        ...validBugReport,
        severity: 'INVALID_SEVERITY',
        id: 'test-invalid-severity'
      };
      const bugReportRef = db.collection('bug_reports').doc(invalidBugReport.id);
      
      await firebase.assertFails(bugReportRef.set(invalidBugReport));
    });

    test('should reject bug report with old timestamp', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const oldBugReport = {
        ...validBugReport,
        timestamp: Date.now() - (2 * 60 * 60 * 1000), // 2 hours ago
        id: 'test-old-timestamp'
      };
      const bugReportRef = db.collection('bug_reports').doc(oldBugReport.id);
      
      await firebase.assertFails(bugReportRef.set(oldBugReport));
    });

    test('should reject bug report with invalid app version', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const invalidBugReport = {
        ...validBugReport,
        appVersion: 'invalid-version',
        id: 'test-invalid-version'
      };
      const bugReportRef = db.collection('bug_reports').doc(invalidBugReport.id);
      
      await firebase.assertFails(bugReportRef.set(invalidBugReport));
    });

    test('should allow authenticated user to read their own bug report', async () => {
      const db = testEnv.authenticatedContext(authenticatedUser.uid).firestore();
      const bugReportWithUser = {
        ...validBugReport,
        userId: authenticatedUser.uid,
        id: 'test-read-own'
      };
      
      // First create the bug report
      await db.collection('bug_reports').doc(bugReportWithUser.id).set(bugReportWithUser);
      
      // Then try to read it
      const bugReportRef = db.collection('bug_reports').doc(bugReportWithUser.id);
      await firebase.assertSucceeds(bugReportRef.get());
    });

    test('should not allow user to read other users bug reports', async () => {
      const db1 = testEnv.authenticatedContext('user1').firestore();
      const db2 = testEnv.authenticatedContext('user2').firestore();
      
      const bugReportWithUser1 = {
        ...validBugReport,
        userId: 'user1',
        id: 'test-read-other'
      };
      
      // User1 creates bug report
      await db1.collection('bug_reports').doc(bugReportWithUser1.id).set(bugReportWithUser1);
      
      // User2 tries to read it
      const bugReportRef = db2.collection('bug_reports').doc(bugReportWithUser1.id);
      await firebase.assertFails(bugReportRef.get());
    });

    test('should allow status updates by owner', async () => {
      const db = testEnv.authenticatedContext(authenticatedUser.uid).firestore();
      const bugReportWithUser = {
        ...validBugReport,
        userId: authenticatedUser.uid,
        id: 'test-status-update'
      };
      
      // Create bug report
      const bugReportRef = db.collection('bug_reports').doc(bugReportWithUser.id);
      await bugReportRef.set(bugReportWithUser);
      
      // Update status
      await firebase.assertSucceeds(
        bugReportRef.update({ status: 'RESOLVED' })
      );
    });

    test('should not allow updating fields other than status', async () => {
      const db = testEnv.authenticatedContext(authenticatedUser.uid).firestore();
      const bugReportWithUser = {
        ...validBugReport,
        userId: authenticatedUser.uid,
        id: 'test-invalid-update'
      };
      
      // Create bug report
      const bugReportRef = db.collection('bug_reports').doc(bugReportWithUser.id);
      await bugReportRef.set(bugReportWithUser);
      
      // Try to update title (should fail)
      await firebase.assertFails(
        bugReportRef.update({ title: 'New Title' })
      );
    });
  });

  describe('User Feedback Collection', () => {
    test('should allow anonymous feedback creation with valid data', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const feedbackRef = db.collection('user_feedback').doc(validFeedback.id);
      
      await firebase.assertSucceeds(feedbackRef.set(validFeedback));
    });

    test('should allow authenticated feedback creation', async () => {
      const db = testEnv.authenticatedContext(authenticatedUser.uid).firestore();
      const feedbackWithUser = {
        ...validFeedback,
        userId: authenticatedUser.uid,
        isAnonymous: false,
        id: 'test-feedback-auth'
      };
      const feedbackRef = db.collection('user_feedback').doc(feedbackWithUser.id);
      
      await firebase.assertSucceeds(feedbackRef.set(feedbackWithUser));
    });

    test('should reject feedback with invalid rating', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const invalidFeedback = {
        ...validFeedback,
        rating: 0, // Invalid rating
        id: 'test-invalid-rating'
      };
      const feedbackRef = db.collection('user_feedback').doc(invalidFeedback.id);
      
      await firebase.assertFails(feedbackRef.set(invalidFeedback));
    });

    test('should reject feedback with invalid category', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const invalidFeedback = {
        ...validFeedback,
        category: 'INVALID_CATEGORY',
        id: 'test-invalid-category'
      };
      const feedbackRef = db.collection('user_feedback').doc(invalidFeedback.id);
      
      await firebase.assertFails(feedbackRef.set(invalidFeedback));
    });

    test('should reject feedback with short message', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const invalidFeedback = {
        ...validFeedback,
        message: 'Hi', // Too short
        id: 'test-short-message'
      };
      const feedbackRef = db.collection('user_feedback').doc(invalidFeedback.id);
      
      await firebase.assertFails(feedbackRef.set(invalidFeedback));
    });

    test('should allow authenticated user to read their own non-anonymous feedback', async () => {
      const db = testEnv.authenticatedContext(authenticatedUser.uid).firestore();
      const feedbackWithUser = {
        ...validFeedback,
        userId: authenticatedUser.uid,
        isAnonymous: false,
        id: 'test-read-own-feedback'
      };
      
      // Create feedback
      const feedbackRef = db.collection('user_feedback').doc(feedbackWithUser.id);
      await feedbackRef.set(feedbackWithUser);
      
      // Read it back
      await firebase.assertSucceeds(feedbackRef.get());
    });

    test('should not allow reading anonymous feedback', async () => {
      const db = testEnv.authenticatedContext(authenticatedUser.uid).firestore();
      const anonymousFeedback = {
        ...validFeedback,
        userId: authenticatedUser.uid,
        isAnonymous: true,
        id: 'test-read-anonymous'
      };
      
      // Create anonymous feedback
      const feedbackRef = db.collection('user_feedback').doc(anonymousFeedback.id);
      await feedbackRef.set(anonymousFeedback);
      
      // Try to read it back (should fail)
      await firebase.assertFails(feedbackRef.get());
    });

    test('should not allow feedback updates', async () => {
      const db = testEnv.authenticatedContext(authenticatedUser.uid).firestore();
      const feedbackWithUser = {
        ...validFeedback,
        userId: authenticatedUser.uid,
        isAnonymous: false,
        id: 'test-no-update'
      };
      
      // Create feedback
      const feedbackRef = db.collection('user_feedback').doc(feedbackWithUser.id);
      await feedbackRef.set(feedbackWithUser);
      
      // Try to update (should fail)
      await firebase.assertFails(
        feedbackRef.update({ rating: 5 })
      );
    });
  });

  describe('Legacy Feedback Collection', () => {
    test('should allow legacy feedback creation', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const legacyFeedback = {
        publicKey: 'test-public-key',
        privateKey: 'test-private-key',
        username: 'testuser',
        message: 'Test legacy feedback',
        deviceModel: 'Test Device',
        createdAt: Date.now()
      };
      
      const feedbackRef = db.collection('feedbacks').doc('test-legacy');
      await firebase.assertSucceeds(feedbackRef.set(legacyFeedback));
    });

    test('should not allow reading legacy feedback', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const feedbackRef = db.collection('feedbacks').doc('test-legacy');
      
      await firebase.assertFails(feedbackRef.get());
    });
  });

  describe('Security Edge Cases', () => {
    test('should reject document ID mismatch', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const bugReportWithWrongId = {
        ...validBugReport,
        id: 'correct-id'
      };
      
      // Try to create with different document ID
      const bugReportRef = db.collection('bug_reports').doc('wrong-id');
      await firebase.assertFails(bugReportRef.set(bugReportWithWrongId));
    });

    test('should reject missing required fields', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const incompleteBugReport = {
        id: 'test-incomplete',
        title: 'Test Bug',
        // Missing description and other required fields
      };
      
      const bugReportRef = db.collection('bug_reports').doc(incompleteBugReport.id);
      await firebase.assertFails(bugReportRef.set(incompleteBugReport));
    });

    test('should reject access to unknown collections', async () => {
      const db = testEnv.unauthenticatedContext().firestore();
      const unknownRef = db.collection('unknown_collection').doc('test');
      
      await firebase.assertFails(unknownRef.set({ data: 'test' }));
    });
  });
});