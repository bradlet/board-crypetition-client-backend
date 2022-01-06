# board-crypetition-client-backend
Backend client for Final Project Crypto-based board game competition app

### Implementation
A Ktor backend implementing a WebSockets API for frontend clients,
and communicating with Ethereum via Web3J.

#### Note on dev environment
IntelliJ is causing breakages in test dependency setup regardless of gradle config. This isn't a gradle issue, 
delete your local directory and re-clone, wiping intellij setup, to fix it.