package com.togglebuttons;

/*
* Loads a button's icon image from the local filesystem.
* A file image always takes priority over a searched item icon.
*/

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import lombok.extern.slf4j.Slf4j;

@Slf4j
final class ToggleButtonsImageLoader
{
	private ToggleButtonsImageLoader()
	{
	}

	// Returns null when the path is unset, missing, or unreadable as an image
	static BufferedImage load(String path)
	{
		if (path == null || path.isEmpty())
		{
			return null;
		}

		final File file = new File(path);
		if (!file.isFile())
		{
			log.debug("Button icon image not found: {}", path);
			return null;
		}

		try
		{
			final BufferedImage image = ImageIO.read(file);
			if (image == null)
			{
				log.warn("File is not a readable image: {}", path);
			}
			return image;
		}
		catch (IOException ex)
		{
			log.warn("Failed to read button icon image: {}", path, ex);
			return null;
		}
	}
}
