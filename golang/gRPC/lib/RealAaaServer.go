package lib

import (
	"Mao_Service_Framework/AAA"
	"Mao_Service_Framework/Node"
	context "context"
	pb "Mao_Service_Framework/gRPC/framework.maojianwei.com/api"
)

type RealAaaServer struct {
	pb.UnimplementedAAAServer

	aaaManager *AAA.AaaManager
	nodeManager *Node.NodeManager
}

func (s *RealAaaServer) LinkAaaManager(am *AAA.AaaManager) {
	s.aaaManager = am
}

func (s *RealAaaServer) Login(ctx context.Context, info *pb.LoginInfo) (*pb.LoginResult, error) {
	cookie, err := s.aaaManager.Authenticate(info.GetLocalIp(), info.GetKey())
	if err != nil {
		return &pb.LoginResult{Success: false}, nil
	}
	return &pb.LoginResult{Success: true, Cookie: cookie}, nil
}
