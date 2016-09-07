precision mediump float;
varying vec2 vTextureCoord;
uniform sampler2D sTexture;

void main() {
    vec4 color = texture2D(sTexture, vec2(vTextureCoord.x,vTextureCoord.y+1.0));
    vec4 color2 = vec4(color.x,color.y,color.z,1.0);
    gl_FragColor = color2;
}
