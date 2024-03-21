package mojang

import (
	"context"
	"encoding/json"
	"fmt"
	"net/http"
)

type Client struct {
	httpClient HttpClient
}

func New(httpClient HttpClient) *Client {
	return &Client{
		httpClient: httpClient,
	}
}

func (c *Client) GetMeta(ctx context.Context) (*Meta, error) {
	const requestPath = "https://piston-meta.mojang.com/v1/packages/1ea0afa4d4caba3a752fd8f0725b7b83eb879514/1.20.4.json"

	req, err := http.NewRequestWithContext(ctx, "GET", requestPath, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to construct new request: %w", err)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to get meta: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("failed to get meta: expected http status 200, got %d", resp.StatusCode)
	}

	var v Meta
	if err := json.NewDecoder(resp.Body).Decode(&v); err != nil {
		return nil, fmt.Errorf("failed to unmarshal meta: %w", err)
	}

	return &v, nil
}

func (c *Client) GetAssetIndex(ctx context.Context, url string) (*AssetIndex, error) {
	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return nil, fmt.Errorf("failed to construct new request: %w", err)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return nil, fmt.Errorf("failed to get asset index: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return nil, fmt.Errorf("failed to get asset index: expected http status 200, got %d", resp.StatusCode)
	}

	var v AssetIndex
	if err := json.NewDecoder(resp.Body).Decode(&v); err != nil {
		return nil, fmt.Errorf("failed to unmarshal meta: %w", err)
	}

	return &v, nil

}

func (c *Client) FormatAssetUrl(asset AssetObject) string {
	return fmt.Sprintf("https://resources.download.minecraft.net/%s/%s", asset.Hash[:2], asset.Hash)
}
