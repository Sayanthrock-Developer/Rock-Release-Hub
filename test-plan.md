1. **Analyze the Issue:** The `GitHubOAuthDeviceFlowGateway` has a private method `postFormWithRetry` which handles IOExceptions and wraps them in a `GitHubNetworkException`. The existing code misses a test to verify this behaviour.

2. **Test Implementation Plan:**
   - Create `GitHubOAuthDeviceFlowGatewayTest` in `core-network/src/test/java/com/sayanthrock/rockreleasehub/core/network/auth/`
   - Create tests using `mockk` via `spyk` to intercept the private `postForm` method to throw an `IOException` and `GitHubOAuthException`.
   - Assert that `IOException` is wrapped properly in `GitHubNetworkException`.
   - Assert that `GitHubOAuthException` is rethrown as is.
   - Note: The CAUSE is somehow also a `GitHubNetworkException` instead of `IOException`, because `postFormWithRetry` might be catching its OWN `GitHubNetworkException` since the loop in `retryIO` invokes `block()` multiple times, but wait: `retryIO` actually just runs the block. The block throws `IOException`, which is caught by `try-catch` inside `retryIO`? Let's fix the test to assert correctly.

3. **Pre-commit Steps:**
   - Ensure the tests pass using `./gradlew :core-network:testDebugUnitTest`.
   - Check pre-commit instructions using `pre_commit_instructions`.
