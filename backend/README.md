# Backend Server

## Setup
1. Clone this repo
2. In IntelliJ, right click on the module and set all language levels to `8`.
3. In Intellij settings -> Java Compiler, set bytecode level to `8`.
4. Right click the `main` function located in `App.java`, and run the application.

Testable by going to [this website](https://www.websocket.org/echo.html), using the URL `ws://localhost:4567/sim`.
Messages should be received detailing the moving coordinates of a smart boat.
