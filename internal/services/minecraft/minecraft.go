package minecraft

import (
	"fmt"
	"os"
	"os/exec"
	"path"
	"strings"

	"github.com/QiZD90/qizd-launcher/internal/clients/mojang"
	"github.com/samber/lo"
)

type Service struct{}

func New() *Service {
	return &Service{}
}

func (s *Service) FormatClassPath(meta *mojang.Meta) string {
	paths := lo.Map(meta.Libraries, func(l mojang.Library, _ int) string { return "libraries/" + l.Downloads.Artifact.Path })
	paths = append(paths, "client.jar")

	return strings.Join(paths, ";")
}

func (s *Service) Launch(meta *mojang.Meta) error {
	_, err := os.Getwd()
	if err != nil {
		return fmt.Errorf("failed to get current working directory: %w", err)
	}

	cmd := exec.Command(
		path.Join("jre", "bin", "java"),
		"-cp", s.FormatClassPath(meta),
		meta.MainClass,
		"--assetsDir", "assets",
		"--assetIndex", "1.20.4",
		"--accessToken", "accessToken", "--version", meta.Version)
	cmd.Dir = "minecraft"
	cmd.Stdout = os.Stdout
	cmd.Stderr = os.Stderr

	if err := cmd.Run(); err != nil {
		return fmt.Errorf("failed to run command: %w", err)
	}

	return nil
}
