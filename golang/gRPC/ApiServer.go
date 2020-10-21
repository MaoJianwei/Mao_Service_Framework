package gRPC

import (
	"Mao_Service_Framework/AAA"
	"Mao_Service_Framework/Node"
	"google.golang.org/grpc"
	"log"
	"net"
	pb "Mao_Service_Framework/gRPC/framework.maojianwei.com/api"
	"Mao_Service_Framework/gRPC/lib"
)

const (
	addr = "[::]:9876"
)

type ApiServer struct {
	server *grpc.Server

	aaaServer  lib.RealAaaServer // if have member variate
	nodeServer lib.RealNodeServer
}

func (as *ApiServer) LinkAaaManager(am *AAA.AaaManager) {
	as.aaaServer.LinkAaaManager(am)
}

func (as *ApiServer) LinkNodeManager(nm *Node.NodeManager) {
	as.nodeServer.LinkNodeManager(nm)
}

func (s *ApiServer) StartServer() {
	listener, err := net.Listen("tcp", addr)
	if err != nil {
		log.Printf("Fail to listen %s, %v", addr, err)
	}

	s.server = grpc.NewServer()

	pb.RegisterAAAServer(s.server, &s.aaaServer)

	if err := s.server.Serve(listener); err != nil {
		log.Printf("Fail to serve, %v", err)
	}
	log.Printf("Serve ok")
}

func (s *ApiServer) StopServer() {

}
