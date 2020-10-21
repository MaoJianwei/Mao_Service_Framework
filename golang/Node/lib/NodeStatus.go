package lib

type NodeStatus struct {
	Cpu float64
	CpuTemp float64
	GpuTemp float64
	EnvTemp float64
	SysTime string
}

func NewNodeStatus(cpu float64, cpuTemp float64, gpuTemp float64, envTemp float64, sysTime string) *NodeStatus {
	return &NodeStatus{Cpu: cpu, CpuTemp: cpuTemp, GpuTemp: gpuTemp, EnvTemp: envTemp, SysTime: sysTime}
}

