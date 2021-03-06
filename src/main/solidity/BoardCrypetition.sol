pragma solidity >=0.8.0 <= 0.8.10;

// SPDX-License-Identifier: UNLICENSED

// @title Board Crypetition
// @author bradlet - Bradley Thompson
// @notice This essentially an escrow smart contract designed for efficient 'transaction' lookup. Focused on games.
contract BoardCrypetition {
    // constants
    enum GameState { NOT_INITIALIZED, INITIALIZED, READY, COMPLETED }
    uint8 private constant INITIALIZED = uint8(GameState.INITIALIZED);

    // contract storage variables
    address owner;
    address server;
    uint256 public totalCollectedFees;
    uint8 public feePercent;
    uint8 recentLobbyDepth;

    Lobby[] lobbies;
    mapping(address => uint128) currentGameMap; // address to their current gameId -- gameId 0 == none active.
    mapping(uint128 => uint128) private gameIdIndexMap; // client-side gameId lobby lookup map.

    // Sent when a player joins a game successfully and the lobby's state is updated to READY.
    event GameReady(
        uint128 indexed gameId
    );

    struct Lobby {
        uint128 gameId;
        uint256 wager; // in wei
        address payable player1;
        address payable player2;
        uint8 gameState; // uint8 state codes -- src/main/kotlin/com/bradlet/models/GameState.kt for more info.
    }

    constructor (address _server, uint8 _initialFee, uint8 _initialRecentLobbyDepth) payable hasPercentInput(_initialFee) {
        // Initial values
        owner = msg.sender;
        server = _server;
        totalCollectedFees += msg.value;
        feePercent = _initialFee;
        recentLobbyDepth = _initialRecentLobbyDepth;

        lobbies.push(); // 0th element is going to be empty, using 0 index to denote absence elsewhere.
    }

    function setFee(uint8 _fee) external onlyOwner hasPercentInput(_fee) {
        feePercent = _fee;
    }

    function setServerAddress(address _server) external onlyOwner {
        server = _server;
    }

    function setRecentLobbyDepth(uint8 _depth) external onlyServer {
        recentLobbyDepth = _depth;
    }

    // Called by frontend clients to create an INITIALIZED game lobby, ready for another player to join.
    function createGame(uint128 _gameId) external payable onlyOneConcurrentGame {
        require(msg.value >= .001 ether, "cannot create game with wager less than .001 ether");
        require(gameIdIndexMap[_gameId] == 0, "provided gameId already exists");

        // Initialize new game lobby with player2 as zero-address. In this app, that equates to null.
        Lobby memory newGameLobby = Lobby(_gameId, msg.value, payable(msg.sender), payable(0), INITIALIZED);
        currentGameMap[msg.sender] = _gameId;
        gameIdIndexMap[_gameId] = getNextGameIndex();
        lobbies.push(newGameLobby);
    }

    // Called by frontend clients to join an INITIALIZED game, and informing consumers of GameReady (the server)
    // that the game may begin.
    function joinGame(uint128 _gameId) external payable onlyOneConcurrentGame {
        uint128 gameIndex = lookupGameIndex(_gameId); // lookupGameIndex ensures game exists
        Lobby storage lobby = lobbies[gameIndex];
        require(lobby.gameState == INITIALIZED, "game with provided ID cannot be joined.");
        require(msg.value == lobby.wager, "must match the lobby's wager when joining a game.");

        currentGameMap[msg.sender] = _gameId;
        lobby.player2 = payable(msg.sender);
        lobby.gameState = uint8(GameState.READY);
        emit GameReady(_gameId);
    }

    // Called only by the game server. Updates game state, collects fees and sends out payment to winner.
    function completeGame(uint128 _gameId, bool _player1Won) external onlyServer {
        uint128 gameIndex = lookupGameIndex(_gameId);
        Lobby storage lobby = lobbies[gameIndex];
        require(lobby.gameState == uint8(GameState.READY), "Only READY lobbies can be completed.");

        // Explicitly point out winner player then collect fees and send payout of remainder to winner.
        address payable winningPlayer = _player1Won ? lobby.player1 : lobby.player2;
        uint256 lobbyFunds = lobby.wager * 2; // 2 players paid the same wager to enter the lobby
        uint256 feesCollected = (feePercent / 100) * lobbyFunds;

        // Send the remainder of the winner's pot, after fee collection.
        assert(winningPlayer.send(lobbyFunds - feesCollected) == true);

        // Remove players from the game
        currentGameMap[lobby.player1] = 0;
        currentGameMap[lobby.player2] = 0;
        lobby.gameState = uint8(GameState.COMPLETED);
    }

    // Returns all lobbies that are in an INITIALIZED state (code: 1), these are games awaiting a second player.
    // Capping amount to show for simplicity -- games can become hard to discover if they are old, as a result.
    function getRecentOpenLobbies() external view returns(uint128[] memory) {
        uint128[] memory openLobbies = new uint128[](recentLobbyDepth);
        uint8 currentOpenLobbyCount = 0;
        // Loop (most recent first) through all lobbies, or until `recentLobbyDepth` open lobbies have been found.
        // Ignores 0th element because it is initialized in the constructor as an empty Lobby.
        for (uint i = lobbies.length-1; i > 0 && currentOpenLobbyCount < recentLobbyDepth; i--) {
            uint8 lobbyState = lobbies[i].gameState;
            uint128 lobbyId = lobbies[i].gameId;
            // if game state == INITIALIZED add this lobby's gameId to openLobbies
            if (lobbyState == INITIALIZED) {
                openLobbies[currentOpenLobbyCount++] = lobbyId;
            }
        }
        return openLobbies;
    }

    // Returns an unpacked representation
    function findGameLobby(uint128 _gameId) external view returns(uint128, uint256, address, address, uint8) {
        Lobby memory lobby = lobbies[lookupGameIndex(_gameId)];
        return (lobby.gameId, lobby.wager, lobby.player1, lobby.player2, lobby.gameState);
    }

    // Redundant with findGameLobby, but just a bit cleaner to interact with.
    function lookupGameState(uint128 _gameId) external view returns(uint8) {
        return lobbies[lookupGameIndex(_gameId)].gameState;
    }

    // Leaving the option to get rid of the contract if I want to change things and redeploy a better version later.
    function retireContract() external onlyOwner{
        selfdestruct(payable(owner));
    }

    // If people wanna send the contract money for free, hey, I ain't complaining!
    receive() external payable {}
    fallback() external payable {}

    // --- private helper functions here ---

    function getNextGameIndex() private view returns(uint128) {
        return uint128(lobbies.length);
    }

    // Basically tags require exists check on top of gameIdIndexMap reads
    function lookupGameIndex(uint128 _gameId) private view returns(uint128) {
        uint128 lobbyIndex = gameIdIndexMap[_gameId];
        require(lobbyIndex != 0, "No lobby exists with provided game ID");
        return lobbyIndex ;
    }

    // --- All modifiers below this point ---

    // Simple enough that I could just do it w/o a source, but including Sol doc reference just in case:
    // https://docs.soliditylang.org/en/v0.8.10/contracts.html?highlight=virtual#function-modifiers
    modifier onlyOwner {
        require(msg.sender == owner, "Only owner can call this function.");
        _;
    }

    modifier onlyServer {
        require(msg.sender == server, "Only the game server can call this function.");
        _;
    }

    modifier onlyOneConcurrentGame {
        require(currentGameMap[msg.sender] == 0, "sender is already in a game");
        _;
    }

    modifier hasPercentInput(uint8 _input) {
        require(_input >= 0 && _input <= 100, "Input is not a valid percentage int");
        _;
    }
}