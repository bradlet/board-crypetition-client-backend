pragma solidity >=0.8.0 <= 0.8.10;

// @title Board Crypetition
// @author bradlet - Bradley Thompson
// @notice This essentially an escrow smart contract designed for efficient 'transaction' lookup. Focused on games.
contract BoardCrypetition {
    // constants
    uint256 private constant recentLobbiesToShow = 100;

    // contract storage variables
    address owner;
    address server;
    uint256 public totalCollectedFees;
    uint8 public feePercent;

    Lobby[] lobbies;
    mapping(address => uint256) currentGameMap; // An address can only be in one game at a time. 0 means no game active
    mapping(uint256 => uint256) private gameIdIndexMap; // client-side gameId lobby lookup map.

    struct Lobby {
        uint256 gameId;
        uint256 winnersPot; // amount to pay to winner in wei
        address payable player1;
        address payable player2;
        uint8 gameState; // uint8 state codes -- src/main/kotlin/com/bradlet/models/GameState.kt for more info.
    }

    constructor (address _server, uint8 _initialFee) payable hasPercentInput(_initialFee) {
        // Initial values
        owner = msg.sender;
        server = _server;
        totalCollectedFees += msg.value;
        feePercent = _initialFee;

        lobbies.push(); // 0th element is going to be empty, using 0 index to denote absence elsewhere.
    }

    function setFee(uint8 _fee) external onlyOwner hasPercentInput(_fee) {
        feePercent = _fee;
    }

    function setServerAddress(address _server) external onlyOwner {
        server = _server;
    }

    // Returns all lobbies that are in an INITIALIZED state (code: 1), these are games awaiting a second player.
    // Capping amount to show for simplicity -- games can become hard to discover if they are old, as a result.
    function getRecentOpenLobbies() external view returns(uint256[recentLobbiesToShow] memory) {
        uint256[recentLobbiesToShow] memory openLobbies;
        uint256 currentOpenLobbyCount = 0;
        // Loop (most recent first) through all lobbies, or until 'recentLobbiesToShow' open lobbies have been found.
        for (uint256 i = lobbies.length-1; i >= 0 && currentOpenLobbyCount < recentLobbiesToShow; i--) {
            Lobby memory lobby = lobbies[i];
            // if game state == INITIALIZED add this lobby's gameId to openLobbies
            if (lobby.gameState == 1) {
                openLobbies[currentOpenLobbyCount] = lobby.gameId;
                currentOpenLobbyCount += 1;
            }
        }
        return openLobbies;
    }

    // Returns an unpacked representation
    function findGameLobby(uint256 _gameId) external view returns(uint256, uint256, address, address, uint8) {
        Lobby memory lobby = lobbies[lookupGameIndex(_gameId)];
        return (lobby.gameId, lobby.winnersPot, lobby.player1, lobby.player2, lobby.gameState);
    }

    // Redundant with findGameLobby, but just a bit cleaner to interact with.
    function lookupGameState(uint256 _gameId) external view returns(uint8) {
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

    function getNextGameIndex() private view returns(uint256) {
        return lobbies.length;
    }

    // Basically tags require exists check on top of gameIdIndexMap reads
    function lookupGameIndex(uint256 _gameId) private view returns(uint256) {
        uint256 lobbyIndex = gameIdIndexMap[_gameId];
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

    modifier hasPercentInput(uint8 _input) {
        require(_input >= 0 && _input <= 100, "Input is not a valid percentage int");
        _;
    }
}