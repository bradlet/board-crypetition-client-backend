pragma solidity 0.8.0;

contract MyContract {
    address public owner;

    constructor () payable {
        owner = msg.sender;
    }

    function getBalance() external view returns(uint) {
        return address(this).balance;
    }

    function cashOut() external {
        selfdestruct(payable(owner));
    }

    fallback() external payable {}
}