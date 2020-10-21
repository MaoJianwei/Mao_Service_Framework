package lib

import "fmt"

type Node struct {
	nodeId NodeId
	state  NodeState
}

func NewNode(nodeId NodeId) *Node {
	return &Node{nodeId: nodeId, state: NODE_DOWN}
}

func (n Node) String() string {
	return fmt.Sprintf("Node{ID: %s, State: %d}", n.nodeId, n.state)
}

func (n *Node) GoUp() {
	n.state = NODE_UP
}

func (n *Node) GoDown() {
	n.state = NODE_DOWN
}

func (n *Node) GetNodeId() NodeId {
	return n.nodeId
}

func (n *Node) GetNodeState() NodeState {
	return n.state
}
