package lib

import "fmt"

type DeviceId struct {
	deviceIdStr string
}

func NewDeviceId(id string) *DeviceId {
	return &DeviceId{deviceIdStr: id}
}

func (deviceId DeviceId) String() string {
	return fmt.Sprintf("%s", deviceId.deviceIdStr)
}