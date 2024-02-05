package main

import (
	"context"
	"log"
	"net/http"

	"github.com/QiZD90/qizd-launcher/internal/clients/adoptium"
	"github.com/QiZD90/qizd-launcher/internal/clients/downloader"
)

func main() {
	ctx := context.Background()

	httpClient := http.DefaultClient
	adoptiumClient := adoptium.New(httpClient)
	downloaderClient := downloader.New(httpClient)

	link, err := adoptiumClient.LatestAssetsLink(ctx, "windows", "x64")
	if err != nil {
		log.Fatal(err)
	}

	err = downloaderClient.DownloadAndUnarchive(ctx, link, "./jre", downloader.ExtractRootFolder)
	if err != nil {
		log.Fatal(err)
	}
}
