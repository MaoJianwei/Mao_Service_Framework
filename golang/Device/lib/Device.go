package lib

import "fmt"

type Device struct {
	deviceId DeviceId
	state DeviceState
}

func NewDevice(deviceId DeviceId) *Device {
	return &Device{deviceId: deviceId, state: DEVICE_DOWN}
}

func (d Device) String() string {
	return fmt.Sprintf("Device{ID: %s, State: %d}", d.deviceId, d.state)
}

func (d *Device) GoUp() {
	d.state = DEVICE_UP
}

func (d *Device) GoDown() {
	d.state = DEVICE_DOWN
}

func (d *Device) GetDeviceId() DeviceId {
	return d.deviceId
}

func (d *Device) GetDeviceState() DeviceState {
	return d.state
}
