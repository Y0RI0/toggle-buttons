package com.togglebuttons;

/*
* Simple class to hold the parameters of a single button target
*/

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
class ToggleButtonsTarget
{
	private String pluginName;
	private boolean disablePlugin;
}
