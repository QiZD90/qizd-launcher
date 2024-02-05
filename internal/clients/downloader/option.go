package downloader

type Options struct {
	extractRootFolder bool
}

type Option func(*Options)

func ExtractRootFolder(o *Options) {
	o.extractRootFolder = true
}
