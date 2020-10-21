package lib

import "fmt"

type NodeId struct {
	nodeIdStr string
}

func NewNodeId(id string) *NodeId {
	return &NodeId{nodeIdStr: id}
}

func (nodeId NodeId) String() string {
	return fmt.Sprintf("%s", nodeId.nodeIdStr)
}
