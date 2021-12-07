pragma solidity >=0.8.0 <= 0.8.10;

contract MyContract {
    address public owner;

    constructor () payable {
        owner = msg.sender;
    }

    function getBalance() external view returns(uint) {
        return address(this).balance;
    }

    function getQuadruple() external pure returns(string memory, uint8, address, address) {
        address player1 = address(0x3585C484fd0e9748Acfe3f10d493847Ad64A8D5b);
        address player2 = address(0x3585C484fd0e9748Acfe3f10d493847Ad64A8D5b);
        return ("test", 1, player1, player2);
    }

    function cashOut() external {
        selfdestruct(payable(owner));
    }

    receive() external payable {}

    fallback() external payable {}
}