package lib

import (
	"Mao_Service_Framework/AAA"
	"Mao_Service_Framework/Node"
	"Mao_Service_Framework/Node/lib"
	pb "Mao_Service_Framework/gRPC/framework.maojianwei.com/api"
	context "context"
)

type RealNodeServer struct {
	pb.UnimplementedNodeServer

	aaaManager *AAA.AaaManager
	nodeManager *Node.NodeManager
}

func (s *RealNodeServer) LinkAaaManager(am *AAA.AaaManager) {
	s.aaaManager = am
}

func (s *RealNodeServer) LinkNodeManager(nm *Node.NodeManager) {
	s.nodeManager = nm
}

func (s *RealNodeServer) checkManagers() (bool) {
	return s.aaaManager != nil && s.nodeManager != nil
}

func (s *RealNodeServer) ReportFeature(ctx context.Context, feature *pb.NodeFeature) (*pb.OkFlag, error) {
	if !s.checkManagers() {
		return &pb.OkFlag{Success: false}, nil
	}
	if !s.aaaManager.IsAuthorized(feature.GetCookie()) {
		return &pb.OkFlag{Success: false}, nil
	}
	if s.nodeManager.RefreshFeature(&lib.NodeFeature{Name: feature.GetName(), IPs: feature.GetIPs()}) {
		return &pb.OkFlag{Success: true}, nil
	}
	return &pb.OkFlag{Success: false}, nil
}

func (s *RealNodeServer) ReportStatus(ctx context.Context, status *pb.NodeStatus) (*pb.OkFlag, error) {
	if !s.checkManagers() {
		return &pb.OkFlag{Success: false}, nil
	}
	if !s.aaaManager.IsAuthorized(status.GetCookie()) {
		return &pb.OkFlag{Success: false}, nil
	}
	if s.nodeManager.RefreshStatus(lib.NewNodeStatus(
		status.GetCpu(), status.GetCpuTemp(), status.GetGpuTemp(), status.GetEnvTemp(), status.GetSysTime())) {
		return &pb.OkFlag{Success: true}, nil
	}
	return &pb.OkFlag{Success: false}, nil
}