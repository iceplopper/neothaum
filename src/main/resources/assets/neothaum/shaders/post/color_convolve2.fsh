#version 330
#extension GL_ARB_separate_shader_objects : require

// TC4's color_convolve2: colour scale, then saturation (desaturatetc and hunger)

uniform sampler2D InSampler;

layout(std140) uniform ConvolveConfig {
    vec3 ColorScale;
    float Saturation;
};

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const vec3 Gray = vec3(0.3, 0.59, 0.11);

void main() {
    vec4 InTexel = texture(InSampler, texCoord);
    vec3 OutColor = InTexel.rgb * ColorScale;
    float Luma = dot(OutColor, Gray);
    vec3 Chroma = OutColor - Luma;
    OutColor = (Chroma * Saturation) + Luma;
    fragColor = vec4(OutColor, 1.0);
}
