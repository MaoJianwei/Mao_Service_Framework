package main

import (
	"Mao_Service_Framework/Device"
	"Mao_Service_Framework/Device/lib"
	"log"
)

func main() {
	log.Printf("%v, %v\n", lib.DEVICE_UP, lib.DEVICE_DOWN)

	deviceManager := Device.NewDeviceManager()
	devices := deviceManager.GetAllDevices()
	log.Printf("%v", devices)

	deviceManager.RegisterDevice(*lib.NewDeviceId("beijing-radar-usb"))
	deviceManager.RegisterDevice(*lib.NewDeviceId("shanghai-radar-i2c"))
	d := deviceManager.GetDevice(*lib.NewDeviceId("shanghai-radar-i2c"))
	if d != nil {
		d.GoUp()
	}
	devices = deviceManager.GetAllDevices()
	log.Printf("%v", devices)
	//log.Printf("%v, %v, %v\n", deviceManager.State, deviceManager.Devices, len(deviceManager.Devices))

}

