package AAA

import "fmt"

type AaaManager struct {
	cookies map[string]bool // use map for hash-lookup. bool is useless.

	testCount uint32
}

func NewAaaManager() *AaaManager {
	return &AaaManager{}
}

func (am *AaaManager) Authenticate(remoteIp string, key string) (string, error) {
	// TODO
	am.testCount++
	cookie := fmt.Sprintf("TODO-cookie-%d", am.testCount)
	am.cookies[cookie] = true
	return cookie, nil
}

func (am *AaaManager) IsAuthorized(cookie string) (bool) {
	valid,present := am.cookies[cookie]
	if !present {
		return false
	}
	if !valid {
		return false
	}
	return true
}
