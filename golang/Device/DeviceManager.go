package Device

import "Mao_Service_Framework/Device/lib"

type DeviceManager struct {
	devices map[lib.DeviceId]*lib.Device
}

func NewDeviceManager() *DeviceManager {
	return &DeviceManager{devices: map[lib.DeviceId]*lib.Device{}}
}

func (dm *DeviceManager) RegisterDevice(deviceId lib.DeviceId) {
	dm.devices[deviceId] = lib.NewDevice(deviceId)
}

func (dm *DeviceManager) UnregisterDevice(device *lib.Device) {
	delete(dm.devices, device.GetDeviceId())
}

func (dm *DeviceManager) GetAllDevices() []*lib.Device {
	newDevices := make([]*lib.Device, 0)
	for _,v := range dm.devices {
		newDevices = append(newDevices, v)
	}
	return newDevices
}

func (dm *DeviceManager) GetDevice(deviceId lib.DeviceId) *lib.Device {
	for _, d := range dm.devices {
		if d.GetDeviceId() == deviceId {
			return d
		}
	}
	return nil
}










