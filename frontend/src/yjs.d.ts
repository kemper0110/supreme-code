declare module "y-webrtc" {
  export class WebrtcProvider {
    constructor(roomName: string, doc: Doc, options: { signaling: string[] });
    connect(): void;
    disconnect(): void;
    destroy(): void;
  }
}
