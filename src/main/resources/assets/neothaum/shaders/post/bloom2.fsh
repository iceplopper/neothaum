#version 330
#extension GL_ARB_separate_shader_objects : require

// TC4's bloom2 (sunscorned, second pass): a faint glow around bright areas

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(location = 0) out vec4 fragColor;

const float blurWidth = 0.002;
const float blurSteps = 0.1;
const float amount = 5.0;

void main() {
    vec4 color = texture(InSampler, texCoord);
    float brightness = (color.r + color.g + color.b) / 3.0;
    vec4 sum = vec4(0.0);
    const float foo = 10.0 / (((blurSteps * 2.0) + 1.0) * 4.0);
    for (float i = -blurSteps; i < blurSteps; i++) {
        sum += texture(InSampler, texCoord + vec2(i, i) * blurWidth * 0.7);
        sum += texture(InSampler, texCoord + vec2(i, -i) * blurWidth * 0.7);
        sum += texture(InSampler, texCoord + vec2(0.0, i) * blurWidth);
        sum += texture(InSampler, texCoord + vec2(i, 0.0) * blurWidth);
    }
    sum *= foo;
    vec4 modifier = sum * sum * (0.015 - (brightness * 0.01));
    vec4 outputColor = modifier * amount + color;
    fragColor = vec4((outputColor * 0.9).rgb, 1.0);
}
