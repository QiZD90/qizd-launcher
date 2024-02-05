package downloader

import (
	"archive/tar"
	"compress/gzip"
	"context"
	"errors"
	"fmt"
	"io"
	"net/http"
	"os"
	"path"
	"strings"
)

type Client struct {
	httpClient HttpClient
}

func New(httpClient HttpClient) *Client {
	return &Client{
		httpClient: httpClient,
	}
}

func (c *Client) DownloadAndUnarchive(ctx context.Context, url string, outdir string, opts ...Option) error {
	options := Options{}
	for _, option := range opts {
		option(&options)
	}

	unarchiveFunc := c.unarchiveZip
	switch {
	case strings.HasSuffix(url, ".tar.gz"):
		unarchiveFunc = c.unarchiveTarGz
	case strings.HasSuffix(url, ".zip"):
		unarchiveFunc = c.unarchiveZip
	default:
		return fmt.Errorf("failed to download and unarchive: unknown archive type")
	}

	req, err := http.NewRequestWithContext(ctx, "GET", url, nil)
	if err != nil {
		return fmt.Errorf("failed to construct request: %w", err)
	}

	resp, err := c.httpClient.Do(req)
	if err != nil {
		return fmt.Errorf("failed to download and unarchive: %w", err)
	}
	defer resp.Body.Close()

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("failed to download and unarchive: expected http status 200, got %d", resp.StatusCode)
	}

	return unarchiveFunc(ctx, resp.Body, outdir, options)
}

func (c *Client) unarchiveZip(ctx context.Context, r io.Reader, outdir string, options Options) error {
	return nil
	//var b bytes.Buffer
	//zip.NewReader(&b.)
}

func (c *Client) unarchiveTarGz(ctx context.Context, r io.Reader, outdir string, options Options) error {
	gzipReader, err := gzip.NewReader(r)
	if err != nil {
		return fmt.Errorf("failed to unarchive tar gz: %w", err)
	}

	firstDir := ""
	tarReader := tar.NewReader(gzipReader)
	for header, err := tarReader.Next(); true; header, err = tarReader.Next() {
		if err != nil {
			if errors.Is(err, io.EOF) {
				break
			}

			return fmt.Errorf("failed to extract tar: %w", err)
		}

		switch header.Typeflag {
		case tar.TypeDir:
			if firstDir == "" {
				firstDir = header.Name
			}

			outpath := path.Join(outdir, header.Name)
			if options.extractRootFolder && firstDir != "" && strings.HasPrefix(header.Name, firstDir) {
				s, _ := strings.CutPrefix(header.Name, firstDir)
				outpath = path.Join(outdir, s)
				fmt.Println(header.Name, outpath, firstDir)
			}

			if err := os.MkdirAll(outpath, 0777); err != nil {
				return fmt.Errorf("failed to create directory: %w", err)
			}

		case tar.TypeReg:
			outpath := path.Join(outdir, header.Name)
			if options.extractRootFolder && firstDir != "" && strings.HasPrefix(header.Name, firstDir) {
				s, _ := strings.CutPrefix(header.Name, firstDir)
				outpath = path.Join(outdir, s)
			}

			file, err := os.Create(outpath)
			if err != nil {
				return fmt.Errorf("failed to create file: %w", err)
			}

			if _, err := io.Copy(file, tarReader); err != nil {
				return fmt.Errorf("failed to write to file: %w", err)
			}

			file.Close()
		}
	}

	return nil
}
