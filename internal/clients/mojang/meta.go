package mojang

type Meta struct {
	Downloads struct {
		Client struct {
			Sha1 string `json:"sha1"`
			Size int64  `json:"size"`
			Url  string `json:"url"`
		} `json:"client"`
	} `json:"downloads"`
	AssetIndex struct {
		Url string `json:"url"`
	} `json:"assetIndex"`
	Libraries []Library `json:"libraries"`
	MainClass string    `json:"mainClass"`
}

type Library struct {
	Downloads struct {
		Artifact struct {
			Path string `json:"path"`
			Sha1 string `json:"sha1"`
			Size int64  `json:"size"`
			Url  string `json:"url"`
		} `json:"artifact"`
	} `json:"downloads"`

	Rules []Rule `json:"rules"`
}

type Rule struct {
	Action string `json:"action"`
	Os     struct {
		Name string `json:"name"`
	} `json:"os"`
}

type AssetIndex struct {
	Objects map[string]AssetObject `json:"objects"`
}

type AssetObject struct {
	Hash string `json:"hash"`
}
