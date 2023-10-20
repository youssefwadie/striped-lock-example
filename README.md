## Single Lock Approach

In the single lock approach, a single `Lock` is used to control access to the critical section where `PayloadBuilder` is accessed. Here's how it works:

1. Each thread tries to acquire the single lock using `singleLock.lock()`.
2. Once a thread acquires the lock, it uses the shared `PayloadBuilder` instance.

## Striped Lock Approach

In the striped lock approach, a `Striped<Lock>` is used to manage individual locks for `userId`. Here's how it works:

1. Each thread attempts to acquire its individual lock using `stripedLock.get(userId)`.
2. Once a thread acquires its lock, it uses the `PayloadBuilder` instance.


### Why the Striped Lock Approach is not correct in this context?

- The striped lock approach is not suitable for this use case because it is designed for scenarios where multiple resources or objects can be locked independently. In this case, each thread's `PayloadBuilder` should not be considered an independent resource, as the shared state of `PayloadBuilder` can still lead to concurrent modifications and `IllegalArgumentException` errors.

- For example, a thread with `userId` = 1 might acquire its lock (lock1), and if another thread with `userId` = 1000 tries to acquire its lock (lock1000), it will succeed. This is because, according to the striped lock, `userId` 1 and `userId` 1000 are considered different keys. But, we only have one instance of the `PayloadBuilder`, and its state (type) can be mutated simultaneously by multiple threads.


### Proposed solutions:
1. Use single lock.
2. Create a new PayloadBuilder each time a thread needs one to eliminate the need for synchronization or locks.
3. Make the PayloadBuilder thread-safe, which in my opinion is not a good design choice because a builder is used to simplify the creation of another complex object, not to handle concurrency.

