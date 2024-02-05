package main

import (
	"context"
	"fmt"
	"log"
	"net/http"
	"path"

	"github.com/QiZD90/qizd-launcher/internal/clients/downloader"
	"github.com/QiZD90/qizd-launcher/internal/clients/mojang"
	"github.com/samber/lo"
)

func main() {
	ctx := context.Background()

	httpClient := http.DefaultClient
	mojangClient := mojang.New(httpClient)
	downloaderClient := downloader.New(httpClient)

	meta, err := mojangClient.GetMeta(ctx)
	if err != nil {
		log.Fatal(err)
	}

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

	/*adoptiumClient := adoptium.New(httpClient)


	link, err := adoptiumClient.LatestAssetsLink(ctx, "windows", "x64")
	if err != nil {
		log.Fatal(err)
	}

	err = downloaderClient.DownloadAndUnarchive(ctx, link, "./jre", downloader.ExtractRootFolder)
	if err != nil {
		log.Fatal(err)
	}*/

	fmt.Println(mojangClient.GetMeta(ctx))
}
