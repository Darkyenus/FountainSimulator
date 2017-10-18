# Fountain Simulator

*Reality:*
![Reality](doc/reality.png)

*Simulation:*
![Simulation](doc/simulation.png)

## Features
- Easy to use visualization of images for the fountain
- Supports multiple bitmap images in arbitrary sequence
- Focus on any part of the sequence, pause it, A-B repeat it
- Accurate image distortion
- Semi-accurate rendering
- Image auto reload

## How to use
- Make sure that you have Java 8 installed
- Download from releases
- Run by double clicking the downloaded .jar file


- Use mouse to drag image files on the screen to add them to the timeline
- Rearrange the image sequence using arrow buttons
- Remove images from the sequence with red `x` button
- Play and pause the animation by clicking the button or by pressing the spacebar
- Scrub on the timeline by dragging with left mouse button
- Create A-B repeat section by dragging with right mouse button on the timeline
- Loaded images that change on disk will reload automatically when changed
	- Supply `-no-reload` argument to disable this
	- Change detection is implemented through last modified timestamp
- Advanced
	- Images on the fountain have their height halved before being displayed.
	This is done automatically by the simulator, but sometimes the image is already
	halved so this manipulation is not desirable. To suppress this,
	supply launch argument `-scale`. If you want to experiment with other scales,
	specify them in the argument like so: `-scale:<scale>`, for example `-scale:0.25`.
	- To enable debug logging, pass `-debug` flag.
	- To enable OpenGL error checking, pass `-gl-debug` flag.

## Credits
See [LICENSE](LICENSE) for author and license information.

Special thanks to *Lukáš Plachý* for kindly supplying image resources and valuable advice.
