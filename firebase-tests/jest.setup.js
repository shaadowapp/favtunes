// Jest setup for Firebase Security Rules testing

// Set test timeout to 30 seconds for Firebase operations
jest.setTimeout(30000);

// Mock console.warn to reduce noise during tests
const originalWarn = console.warn;
beforeAll(() => {
  console.warn = (...args) => {
    if (
      typeof args[0] === 'string' &&
      args[0].includes('@firebase/rules-unit-testing')
    ) {
      return;
    }
    originalWarn(...args);
  };
});

afterAll(() => {
  console.warn = originalWarn;
});