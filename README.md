# Asynchronous Programming in Java SE 17

https://www.pluralsight.com/courses/java-se-17-asynchronous-programming

- Goal is to not block the main thread to increase throughput through your application
- Completion stage api will allow you to pass the result of a task to another process instead of waiting for the result in the main thread
- Completion stage api is implemented by completable future which also implements the future interface 
- Split processing into small tasks, each task doing one thing; chain them with completion stage api
- One task can trigger as many tasks as you want; many down streams 
- You can also trigger a task on the outcome of many tasks
- The completable future api also allows you to fire off multiple async tasks and get the first result which returns first 