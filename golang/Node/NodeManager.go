package Node

import "Mao_Service_Framework/Node/lib"

type NodeManager struct {
	nodes map[lib.NodeId]*lib.Node
}

func NewNodeManager() *NodeManager {
	return &NodeManager{nodes: map[lib.NodeId]*lib.Node{}}
}



func (nm *NodeManager) RefreshFeature(feature *lib.NodeFeature) (bool) {
	return true
}

func (nm *NodeManager) RefreshStatus(status *lib.NodeStatus) (bool) {
	return true
}



func (nm *NodeManager) RegisterNode(nodeId lib.NodeId) {
	nm.nodes[nodeId] = lib.NewNode(nodeId)
}

func (nm *NodeManager) UnregisterNode(node *lib.Node) {
	delete(nm.nodes, node.GetNodeId())
}

func (nm *NodeManager) GetAllNodes() []*lib.Node {
	newNodes := make([]*lib.Node, 0)
	for _,v := range nm.nodes {
		newNodes = append(newNodes, v)
	}
	return newNodes
}

func (nm *NodeManager) GetNode(nodeId lib.NodeId) *lib.Node {
	for _, n := range nm.nodes {
		if n.GetNodeId() == nodeId {
			return n
		}
	}
	return nil
}
