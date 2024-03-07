package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"os"
	"path"
	"slices"

	"github.com/QiZD90/qizd-launcher/internal/clients/adoptium"
	"github.com/QiZD90/qizd-launcher/internal/clients/downloader"
	"github.com/QiZD90/qizd-launcher/internal/clients/mojang"
	"github.com/samber/lo"
)

func main() {
	ctx := context.Background()

	httpClient := http.DefaultClient
	adoptiumClient := adoptium.New(httpClient)
	mojangClient := mojang.New(httpClient)
	downloaderClient := downloader.New(httpClient)

	if slices.Contains(os.Args, "jre") {
		link, err := adoptiumClient.LatestAssetsLink(ctx, "windows", "x64")
		if err != nil {
			log.Fatal(err)
		}

		err = downloaderClient.DownloadAndUnarchive(ctx, link, "./jre", downloader.ExtractRootFolder)
		if err != nil {
			log.Fatal(err)
		}
	}

	meta, err := mojangClient.GetMeta(ctx)
	if err != nil {
		log.Fatal(err)
	}

	if slices.Contains(os.Args, "libraries") {
		downloaderClient.DownloadBatch(ctx, lo.Map(
			meta.Libraries,
			func(l mojang.Library, _ int) downloader.Resource {
				return downloader.Resource{
					Url:     l.Downloads.Artifact.Url,
					Outpath: path.Join(".", "minecraft", "libraries", l.Downloads.Artifact.Path),
				}
			},
		), downloader.WithCallback(func(r downloader.Resource) {
			fmt.Println(r)
		}))
	}

	if slices.Contains(os.Args, "assets") {
		assetIndex, err := mojangClient.GetAssetIndex(ctx, meta.AssetIndex.Url)
		if err != nil {
			log.Fatal(err)
		}

		downloaderClient.DownloadBatch(ctx, lo.MapToSlice(
			assetIndex.Objects,
			func(k string, v mojang.AssetObject) downloader.Resource {
				return downloader.Resource{
					Url:     mojangClient.FormatAssetUrl(v),
					Outpath: path.Join(".", "minecraft", "assets", "objects", v.Hash[:2], v.Hash),
				}
			},
		), downloader.WithCallback(func(r downloader.Resource) {
			fmt.Println(r)
		}))
	}

	fmt.Println(mojangClient.FormatClassPath(meta))
}
