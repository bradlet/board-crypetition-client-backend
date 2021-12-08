pragma solidity >=0.8.0 <= 0.8.10;

contract BoardCrypetition {
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

    function getNextGameIndex() external view returns(uint256) {
        return lobbies.length;
    }

    function setFee(uint8 _fee) external onlyOwner hasPercentInput(_fee) {
        feePercent = _fee;
    }

    function setServerAddress(address _server) external onlyOwner {
        server = _server;
    }

    // Returns an unpacked representation
    function findGameLobby(uint256 _gameId) external view returns(uint256, uint256, address, address, uint8) {
        uint256 lobbyIndex = gameIdIndexMap[_gameId];
        require(lobbyIndex != 0, "No lobby exists with provided game ID");
        Lobby memory lobby = lobbies[lobbyIndex];
        return (lobby.gameId, lobby.winnersPot, lobby.player1, lobby.player2, lobby.gameState);
    }

    // Leaving the option to get rid of the contract if I want to change things and redeploy a better version later.
    function retireContract() external onlyOwner{
        selfdestruct(payable(owner));
    }

    // If people wanna send the contract money for free, hey, I ain't complaining!
    receive() external payable {}
    fallback() external payable {}

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