package adoptium

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
)

const API_BASE_URL = "https://api.adoptium.net"

type Client struct {
	httpClient HttpClient
}

func New(httpClient HttpClient) *Client {
	return &Client{
		httpClient: httpClient,
	}
}

func (c *Client) LatestAssetsLink(ctx context.Context, os string, arch string) (string, error) {
	const (
		featureVersion    = 21
		releaseType       = "ga"
		imageType         = "jre"
		jvmImpl           = "hotspot"
		heapSize          = "normal"
		vendor            = "eclipse"
		requestPathFormat = "%s/v3/assets/latest/%d/%s?architecture=%s&image_type=%s&os=%s&vendor=%s"
	)

	requestPath := fmt.Sprintf(requestPathFormat, API_BASE_URL, featureVersion, jvmImpl, arch, imageType, os, vendor)

	req, err := http.NewRequestWithContext(ctx, "GET", requestPath, nil)
	if err != nil {
		return "", fmt.Errorf("failed to construct request: %w", err)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return "", fmt.Errorf("failed to get latest assets: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return "", fmt.Errorf("failed to get latest assets: expected http status 200, got %d", resp.StatusCode)
	}

	var v []struct {
		Binary struct {
			Package struct {
				Link string `json: "link"`
			} `json:"package"`
		} `json:"binary"`
	}

	if err := json.NewDecoder(resp.Body).Decode(&v); err != nil {
		return "", fmt.Errorf("failed to unmarshal latest assets response: %w", err)
	}

	if len(v) == 0 {
		return "", fmt.Errorf("failed to find latest assets")
	}

	return v[0].Binary.Package.Link, nil
}
