#version 330 core
//
// Pass through Vertex shader.  
// Passes vertex information through without changing it.
//  Being used for debugging purposes.
// 
uniform mat4 modelingMatrix;

in vec4 vColor;
in vec4 vPosition;

out vec4 Color;

void main()
{
	Color = vColor;
    gl_Position = modelingMatrix * vPosition;
}
