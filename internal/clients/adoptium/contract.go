package adoptium

import "net/http"

type HttpClient interface {
	Do(request *http.Request) (*http.Response, error)
}
