package downloader

type Options struct {
	extractRootFolder bool
	callback          func(Resource)
}

type Option func(*Options)

func ExtractRootFolder(o *Options) {
	o.extractRootFolder = true
}

func WithCallback(callback func(Resource)) Option {
	return func(o *Options) {
		o.callback = callback
	}
}
