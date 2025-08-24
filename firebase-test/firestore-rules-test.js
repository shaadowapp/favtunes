const firebase = require('@firebase/rules-unit-testing');
const fs = require('fs');

const PROJECT_ID = 'test-project';
const RULES_FILE = '../firestore.rules';

// Helper function to create test app
function getFirestore(auth) {
  return firebase.initializeTestApp({
    projectId: PROJECT_ID,
    auth: auth
  }).firestore();
}

// Helper function to create admin app
function getAdminFirestore() {
  return firebase.initializeAdminApp({
    projectId: PROJECT_ID
  }).firestore();
}

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
    const validBugReport = {
      title: 'Test Bug',
      description: 'This is a test bug description',
      severity: 'HIGH',
      category: 'UI',
      deviceInfo: {
        deviceModel: 'Test Device',
        osVersion: 'Android 13',
        appVersion: '1.0.0'
      },
      appVersion: '1.0.0',
      timestamp: firebase.firestore.FieldValue.serverTimestamp(),
      userId: 'test-user-id'
    };

    test('should allow authenticated user to create bug report', async () => {
      const db = getFirestore({ uid: 'test-user-id' });
      const bugReportRef = db.collection('bug_reports').doc();
      
      await firebase.assertSucceeds(
        bugReportRef.set(validBugReport)
      );
    });

    test('should allow anonymous user to create valid bug report', async () => {
      const db = getFirestore(null);
      const anonymousBugReport = {
        ...validBugReport,
        userId: null,
        isAnonymous: true
      };
      
      const bugReportRef = db.collection('bug_reports').doc();
      
      await firebase.assertSucceeds(
        bugReportRef.set(anonymousBugReport)
      );
    });

    test('should reject bug report with invalid severity', async () => {
      const db = getFirestore({ uid: 'test-user-id' });
      const invalidBugReport = {
        ...validBugReport,
        severity: 'INVALID_SEVERITY'
      };
      
      const bugReportRef = db.collection('bug_reports').doc();
      
      await firebase.assertFails(
        bugReportRef.set(invalidBugReport)
      );
    });

    test('should reject bug report with missing required fields', async () => {
      const db = getFirestore({ uid: 'test-user-id' });
      const incompleteBugReport = {
        title: 'Test Bug'
        // Missing other required fields
      };
      
      const bugReportRef = db.collection('bug_reports').doc();
      
      await firebase.assertFails(
        bugReportRef.set(incompleteBugReport)
      );
    });

    test('should allow user to read their own bug report', async () => {
      const adminDb = getAdminFirestore();
      const docRef = adminDb.collection('bug_reports').doc('test-doc');
      
      await docRef.set(validBugReport);
      
      const db = getFirestore({ uid: 'test-user-id' });
      
      await firebase.assertSucceeds(
        db.collection('bug_reports').doc('test-doc').get()
      );
    });

    test('should deny user from reading other users bug reports', async () => {
      const adminDb = getAdminFirestore();
      const docRef = adminDb.collection('bug_reports').doc('test-doc');
      
      await docRef.set({
        ...validBugReport,
        userId: 'other-user-id'
      });
      
      const db = getFirestore({ uid: 'test-user-id' });
      
      await firebase.assertFails(
        db.collection('bug_reports').doc('test-doc').get()
      );
    });
  });

  describe('User Feedback Collection', () => {
    const validFeedback = {
      rating: 4,
      category: 'GENERAL',
      message: 'Great app, love the features!',
      deviceInfo: {
        deviceModel: 'Test Device',
        osVersion: 'Android 13',
        appVersion: '1.0.0'
      },
      appVersion: '1.0.0',
      timestamp: firebase.firestore.FieldValue.serverTimestamp(),
      isAnonymous: true,
      userId: 'test-user-id'
    };

    test('should allow authenticated user to create feedback', async () => {
      const db = getFirestore({ uid: 'test-user-id' });
      const feedbackRef = db.collection('user_feedback').doc();
      
      await firebase.assertSucceeds(
        feedbackRef.set(validFeedback)
      );
    });

    test('should allow anonymous user to create valid feedback', async () => {
      const db = getFirestore(null);
      const anonymousFeedback = {
        ...validFeedback,
        userId: null,
        isAnonymous: true
      };
      
      const feedbackRef = db.collection('user_feedback').doc();
      
      await firebase.assertSucceeds(
        feedbackRef.set(anonymousFeedback)
      );
    });

    test('should reject feedback with invalid rating', async () => {
      const db = getFirestore({ uid: 'test-user-id' });
      const invalidFeedback = {
        ...validFeedback,
        rating: 6 // Invalid rating (should be 1-5)
      };
      
      const feedbackRef = db.collection('user_feedback').doc();
      
      await firebase.assertFails(
        feedbackRef.set(invalidFeedback)
      );
    });

    test('should reject feedback with invalid category', async () => {
      const db = getFirestore({ uid: 'test-user-id' });
      const invalidFeedback = {
        ...validFeedback,
        category: 'INVALID_CATEGORY'
      };
      
      const feedbackRef = db.collection('user_feedback').doc();
      
      await firebase.assertFails(
        feedbackRef.set(invalidFeedback)
      );
    });

    test('should allow user to read their own feedback', async () => {
      const adminDb = getAdminFirestore();
      const docRef = adminDb.collection('user_feedback').doc('test-doc');
      
      await docRef.set(validFeedback);
      
      const db = getFirestore({ uid: 'test-user-id' });
      
      await firebase.assertSucceeds(
        db.collection('user_feedback').doc('test-doc').get()
      );
    });

    test('should deny user from reading other users feedback', async () => {
      const adminDb = getAdminFirestore();
      const docRef = adminDb.collection('user_feedback').doc('test-doc');
      
      await docRef.set({
        ...validFeedback,
        userId: 'other-user-id'
      });
      
      const db = getFirestore({ uid: 'test-user-id' });
      
      await firebase.assertFails(
        db.collection('user_feedback').doc('test-doc').get()
      );
    });
  });

  describe('Admin Access', () => {
    test('should allow admin to read all bug reports', async () => {
      // First, set up admin user
      const adminDb = getAdminFirestore();
      await adminDb.collection('admin_users').doc('admin-user-id').set({
        role: 'admin'
      });
      
      // Create a bug report from another user
      await adminDb.collection('bug_reports').doc('test-doc').set({
        title: 'Test Bug',
        description: 'Test description',
        severity: 'HIGH',
        category: 'UI',
        deviceInfo: {},
        appVersion: '1.0.0',
        timestamp: firebase.firestore.FieldValue.serverTimestamp(),
        userId: 'other-user-id'
      });
      
      // Admin should be able to read it
      const db = getFirestore({ uid: 'admin-user-id' });
      
      await firebase.assertSucceeds(
        db.collection('bug_reports').doc('test-doc').get()
      );
    });
  });
});