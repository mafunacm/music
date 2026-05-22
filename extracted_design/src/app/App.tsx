import { useState, useRef, useEffect } from "react";
import {
  Shuffle,
  SkipBack,
  SkipForward,
  Play,
  Pause,
  Repeat,
  Repeat1,
  ChevronDown,
  MoreVertical,
  Star,
  ShoppingBag,
  MapPin,
  Calendar,
  Music2,
  ChevronLeft,
} from "lucide-react";

const C = {
  inactive: "#03DAC5",
  active: "#FFCC00",
  dormant: "#B0B0B0",
  bg: "#0D0D0D",
  text: "#FFFFFF",
  subtext: "#AAAAAA",
};

const TRACKS = [
  { id: 1, title: "Music For Creative People — Chillout Mix 1 hour", artist: "Unknown" },
  { id: 2, title: "y2mate.com - Inspirational Background Music", artist: "Unknown" },
  { id: 3, title: "Chill Music for Productive Work — Deep Focus", artist: "Unknown" },
  { id: 4, title: "SnapSave.io - Relaxing Music with Rain Sounds", artist: "Unknown" },
  { id: 5, title: "Music for deep sleep — Soulful Ambient", artist: "Unknown" },
  { id: 6, title: "y2mate.com - melody_of_dreams", artist: "Unknown" },
  { id: 7, title: "y2mate.com - Smooth Ambient Journey Vol. 2", artist: "Unknown" },
];

// ── Animated spectrum bars ────────────────────────────────────────────────────
function SpectrumBars({ color }: { color: string }) {
  return (
    <div className="flex items-end gap-px" style={{ height: 18, width: 20 }}>
      <style>{`
        @keyframes sb1{0%,100%{height:30%}25%{height:95%}75%{height:55%}}
        @keyframes sb2{0%,100%{height:75%}40%{height:20%}80%{height:100%}}
        @keyframes sb3{0%,100%{height:50%}15%{height:100%}60%{height:30%}}
        @keyframes sb4{0%,100%{height:85%}50%{height:15%}70%{height:65%}}
        .s1{animation:sb1 0.9s ease-in-out infinite}
        .s2{animation:sb2 0.7s ease-in-out infinite}
        .s3{animation:sb3 1.1s ease-in-out infinite}
        .s4{animation:sb4 0.8s ease-in-out infinite}
      `}</style>
      {(["s1","s2","s3","s4"] as const).map((cls) => (
        <div key={cls} className={`${cls} rounded-sm flex-1`} style={{ background: color, minHeight: 3 }} />
      ))}
    </div>
  );
}

// ── Scrolling marquee ─────────────────────────────────────────────────────────
function Marquee({ text, color = "#fff", className = "" }: { text: string; color?: string; className?: string }) {
  const outer = useRef<HTMLDivElement>(null);
  const [scrolls, setScrolls] = useState(false);

  useEffect(() => {
    const el = outer.current;
    if (!el) return;
    setScrolls(el.scrollWidth > el.clientWidth + 2);
  }, [text]);

  return (
    <div ref={outer} className={`overflow-hidden whitespace-nowrap ${className}`} style={{ color }}>
      <style>{`
        @keyframes mq{0%{transform:translateX(0)}100%{transform:translateX(-50%)}}
        .mq-run{animation:mq 11s linear infinite;display:inline-block}
      `}</style>
      {scrolls
        ? <span className="mq-run">{text}&emsp;&emsp;{text}&emsp;&emsp;</span>
        : <span>{text}</span>}
    </div>
  );
}

// ── Wave gradient background ──────────────────────────────────────────────────
function WaveBackground() {
  return (
    <div className="absolute inset-0 overflow-hidden">
      <style>{`
        @keyframes wv1{0%,100%{transform:translateX(0) translateY(0) scale(1.1)}33%{transform:translateX(-4%) translateY(3%) scale(1.15)}66%{transform:translateX(3%) translateY(-2%) scale(1.08)}}
        @keyframes wv2{0%,100%{transform:translateX(0) translateY(0) scale(1.2)}33%{transform:translateX(5%) translateY(-4%) scale(1.1)}66%{transform:translateX(-3%) translateY(3%) scale(1.25)}}
        @keyframes wv3{0%,100%{transform:translateX(0) translateY(0) scale(1.15)}50%{transform:translateX(-5%) translateY(5%) scale(1.2)}}
        .wv1{animation:wv1 9s ease-in-out infinite}
        .wv2{animation:wv2 12s ease-in-out infinite}
        .wv3{animation:wv3 7s ease-in-out infinite}
      `}</style>
      <div className="absolute inset-0" style={{ background:"#0a0a1a" }} />
      <div className="wv1 absolute rounded-full opacity-30" style={{ width:"120%",height:"60%",top:"-10%",left:"-10%",background:"radial-gradient(ellipse at 40% 50%,#03DAC5 0%,transparent 70%)",filter:"blur(40px)" }} />
      <div className="wv2 absolute rounded-full opacity-40" style={{ width:"100%",height:"70%",bottom:"0",right:"-20%",background:"radial-gradient(ellipse at 60% 60%,#5B2D8E 0%,#1a0533 60%,transparent 100%)",filter:"blur(50px)" }} />
      <div className="wv3 absolute rounded-full opacity-20" style={{ width:"60%",height:"40%",top:"30%",left:"20%",background:"radial-gradient(ellipse at 50% 50%,#FFCC00 0%,transparent 70%)",filter:"blur(60px)" }} />
      <div className="absolute inset-0" style={{ background:"rgba(5,5,15,0.55)" }} />
    </div>
  );
}

// ── Album art ─────────────────────────────────────────────────────────────────
function AlbumArt({ size = 240 }: { size?: number }) {
  return (
    <div className="rounded-2xl flex items-center justify-center shrink-0" style={{
      width: size, height: size,
      background: "linear-gradient(135deg,#1a1a2e 0%,#16213e 50%,#0f3460 100%)",
      boxShadow: `0 20px 60px rgba(3,218,197,0.15),0 8px 32px rgba(0,0,0,0.6)`,
    }}>
      <Music2 size={size * 0.3} color={C.inactive} opacity={0.4} />
    </div>
  );
}

// ── Progress bar ──────────────────────────────────────────────────────────────
function ProgressBar({ progress, onChange }: { progress: number; onChange: (v: number) => void }) {
  return (
    <div className="w-full flex flex-col gap-1">
      <div className="relative h-1 rounded-full cursor-pointer" style={{ background:"rgba(255,255,255,0.15)" }}
        onClick={(e) => {
          const rect = e.currentTarget.getBoundingClientRect();
          onChange(((e.clientX - rect.left) / rect.width) * 100);
        }}>
        <div className="absolute inset-y-0 left-0 rounded-full" style={{ width:`${progress}%`, background:`linear-gradient(90deg,${C.inactive},${C.active})` }} />
        <div className="absolute top-1/2 -translate-y-1/2 w-3 h-3 rounded-full" style={{ left:`calc(${progress}% - 6px)`, background:C.active, boxShadow:`0 0 8px ${C.active}` }} />
      </div>
      <div className="flex justify-between text-xs" style={{ color: C.subtext }}>
        <span>1:32</span><span>58:00</span>
      </div>
    </div>
  );
}

// ── Now Playing ───────────────────────────────────────────────────────────────
function NowPlayingScreen({ track, isPlaying, isShuffle, isRepeat, onClose, onPlayPause, onShuffle, onRepeat, onPrev, onNext }: {
  track: typeof TRACKS[0]; isPlaying: boolean; isShuffle: boolean; isRepeat: boolean;
  onClose: () => void; onPlayPause: () => void; onShuffle: () => void; onRepeat: () => void; onPrev: () => void; onNext: () => void;
}) {
  const [progress, setProgress] = useState(14);
  const [isFav, setIsFav] = useState(false);

  return (
    <div className="absolute inset-0 z-30 flex flex-col" style={{ fontFamily:"'Inter',sans-serif" }}>
      <WaveBackground />

      {/* Top bar */}
      <div className="relative z-10 flex items-center justify-between px-5 pt-12 pb-4">
        <button onClick={onClose} className="p-2" style={{ color: C.dormant }}><ChevronDown size={24} /></button>
        <span className="text-xs font-semibold tracking-widest uppercase" style={{ color: C.subtext }}>Now Playing</span>
        <button className="p-2" style={{ color: C.dormant }}><MoreVertical size={20} /></button>
      </div>

      {/* Album art */}
      <div className="relative z-10 flex justify-center px-8 mt-2">
        <AlbumArt size={240} />
      </div>

      {/* Track info + fav */}
      <div className="relative z-10 flex items-start justify-between px-6 mt-5">
        <div className="flex-1 min-w-0 pr-4">
          <p className="font-bold text-lg leading-tight text-white truncate" style={{ fontFamily:"'Space Grotesk',sans-serif" }}>{track.title}</p>
          <p className="text-sm mt-1" style={{ color: C.subtext }}>{track.artist}</p>
        </div>
        <button onClick={() => setIsFav(!isFav)} className="pt-1">
          <Star size={22} fill={isFav ? C.active : "none"} stroke={isFav ? C.active : C.dormant} />
        </button>
      </div>

      {/* Progress */}
      <div className="relative z-10 px-6 mt-4">
        <ProgressBar progress={progress} onChange={setProgress} />
      </div>

      {/* Transport */}
      <div className="relative z-10 flex items-center justify-between px-6 mt-5">
        <button onClick={onShuffle} className="p-2"><Shuffle size={22} color={isShuffle ? C.active : C.inactive} /></button>
        <button onClick={onPrev} className="p-3"><SkipBack size={28} color={C.inactive} fill={C.inactive} /></button>
        <button onClick={onPlayPause} className="w-16 h-16 rounded-full flex items-center justify-center"
          style={{ background: isPlaying ? C.active : C.inactive, boxShadow:`0 0 24px ${isPlaying ? C.active : C.inactive}66` }}>
          {isPlaying ? <Pause size={28} color="#000" fill="#000" /> : <Play size={28} color="#000" fill="#000" style={{ marginLeft:3 }} />}
        </button>
        <button onClick={onNext} className="p-3"><SkipForward size={28} color={C.inactive} fill={C.inactive} /></button>
        <button onClick={onRepeat} className="p-2">
          {isRepeat ? <Repeat1 size={22} color={C.active} /> : <Repeat size={22} color={C.inactive} />}
        </button>
      </div>

      {/* Gig callout */}
      <div className="relative z-10 mx-6 mt-5">
        <div className="flex items-center gap-3 px-4 py-3 rounded-2xl" style={{ background:"rgba(3,218,197,0.08)", border:"1px solid rgba(3,218,197,0.2)" }}>
          <div className="w-8 h-8 rounded-full flex items-center justify-center shrink-0" style={{ background:"rgba(3,218,197,0.15)" }}>
            <MapPin size={14} color={C.inactive} />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-xs font-semibold text-white">Catch me live</p>
            <p className="text-xs" style={{ color: C.subtext }}>Vibes on Main · 17 June 2026</p>
          </div>
          <div className="flex items-center gap-1 px-2 py-1 rounded-full text-xs font-semibold shrink-0" style={{ background: C.inactive, color:"#000" }}>
            <Calendar size={10} /><span>Save</span>
          </div>
        </div>
      </div>

      {/* Buy more */}
      <div className="relative z-10 mx-6 mt-3">
        <button className="w-full flex items-center justify-center gap-2 rounded-2xl py-3 text-sm font-semibold"
          style={{ background:"rgba(255,204,0,0.1)", border:"1px solid rgba(255,204,0,0.25)", color: C.active }}>
          <ShoppingBag size={16} />Buy More Music
        </button>
      </div>

      <div className="flex-1" />
    </div>
  );
}

// ── Mini player bar ───────────────────────────────────────────────────────────
function MiniPlayerBar({ track, isPlaying, isShuffle, isRepeat, onPlayPause, onPrev, onNext, onShuffle, onRepeat, onOpen }: {
  track: typeof TRACKS[0]; isPlaying: boolean; isShuffle: boolean; isRepeat: boolean;
  onPlayPause: () => void; onPrev: () => void; onNext: () => void; onShuffle: () => void; onRepeat: () => void; onOpen: () => void;
}) {
  return (
    <div className="absolute bottom-4 left-3 right-3 z-20" onClick={onOpen}>
      <div className="rounded-2xl overflow-hidden" style={{
        background: "rgba(20,20,28,0.97)",
        backdropFilter: "blur(24px)",
        border: "1px solid rgba(255,255,255,0.07)",
        boxShadow: `0 8px 40px rgba(0,0,0,0.75),0 0 0 1px rgba(3,218,197,0.06)`,
      }}>
        {/* Progress strip — top edge */}
        <div className="h-0.5 w-full" style={{ background:"rgba(255,255,255,0.07)" }}>
          <div className="h-full rounded-full" style={{ width:"14%", background:`linear-gradient(90deg,${C.inactive},${C.active})` }} />
        </div>

        <div className="px-4 pt-3 pb-3" onClick={(e) => e.stopPropagation()}>
          {/* All 5 controls */}
          <div className="flex items-center justify-between">
            <button onClick={onShuffle} className="w-9 h-9 flex items-center justify-center">
              <Shuffle size={17} color={isShuffle ? C.active : C.inactive} />
            </button>
            <button onClick={onPrev} className="w-9 h-9 flex items-center justify-center">
              <SkipBack size={20} color={C.inactive} fill={C.inactive} />
            </button>
            <button onClick={onPlayPause} className="w-12 h-12 rounded-full flex items-center justify-center"
              style={{ background: isPlaying ? C.active : C.inactive, boxShadow:`0 0 16px ${isPlaying ? C.active : C.inactive}55` }}>
              {isPlaying
                ? <Pause size={20} color="#000" fill="#000" />
                : <Play size={20} color="#000" fill="#000" style={{ marginLeft:2 }} />}
            </button>
            <button onClick={onNext} className="w-9 h-9 flex items-center justify-center">
              <SkipForward size={20} color={C.inactive} fill={C.inactive} />
            </button>
            <button onClick={onRepeat} className="w-9 h-9 flex items-center justify-center">
              {isRepeat ? <Repeat1 size={17} color={C.active} /> : <Repeat size={17} color={C.inactive} />}
            </button>
          </div>

          {/* Scrolling title below controls */}
          <div className="mt-2.5 flex items-center gap-2" onClick={onOpen} style={{ cursor:"pointer" }}>
            <div className="w-5 h-5 rounded-md flex items-center justify-center shrink-0" style={{ background:"rgba(3,218,197,0.15)" }}>
              <Music2 size={11} color={C.inactive} />
            </div>
            <div className="flex-1 min-w-0">
              <Marquee text={track.title} color="#fff" className="text-xs font-semibold" />
            </div>
            <ChevronDown size={14} style={{ color: C.dormant, flexShrink: 0 }} />
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Swipeable track row ───────────────────────────────────────────────────────
function TrackRow({ track, active, isPlaying, onSelect }: {
  track: typeof TRACKS[0]; active: boolean; isPlaying: boolean; onSelect: () => void;
}) {
  const [offset, setOffset] = useState(0);
  const [menuOpen, setMenuOpen] = useState(false);
  const startX = useRef<number | null>(null);
  const MENU_W = 80;

  const onTouchStart = (e: React.TouchEvent) => { startX.current = e.touches[0].clientX; };
  const onTouchMove = (e: React.TouchEvent) => {
    if (startX.current === null) return;
    const dx = e.touches[0].clientX - startX.current;
    if (dx < 0) setOffset(Math.max(dx, -MENU_W));
  };
  const onTouchEnd = () => {
    if (offset < -MENU_W / 2) { setOffset(-MENU_W); setMenuOpen(true); }
    else { setOffset(0); setMenuOpen(false); }
    startX.current = null;
  };

  return (
    <div className="relative mb-1 rounded-2xl overflow-hidden">
      {/* Swipe-revealed menu */}
      <div className="absolute right-0 top-0 bottom-0 flex items-center justify-center rounded-r-2xl px-3"
        style={{ width: MENU_W, background:"rgba(255,204,0,0.12)", border:"1px solid rgba(255,204,0,0.2)" }}>
        <button className="text-xs font-semibold" style={{ color: C.active }} onClick={() => { setOffset(0); setMenuOpen(false); }}>
          More
        </button>
      </div>

      {/* Track row */}
      <div
        className="relative flex items-center gap-3 px-3 py-2.5 rounded-2xl"
        style={{
          transform: `translateX(${offset}px)`,
          transition: startX.current !== null ? "none" : "transform 0.25s ease",
          background: active ? `rgba(255,204,0,0.07)` : "rgba(18,18,18,0.8)",
          border: active ? `1px solid rgba(255,204,0,0.2)` : "1px solid rgba(255,255,255,0.04)",
          cursor: "pointer",
        }}
        onClick={() => { if (offset === 0) onSelect(); else { setOffset(0); setMenuOpen(false); } }}
        onTouchStart={onTouchStart}
        onTouchMove={onTouchMove}
        onTouchEnd={onTouchEnd}
      >
        {/* Swipe hint chevron — left edge */}
        <ChevronLeft size={13} style={{ color: C.dormant, opacity: 0.35, flexShrink: 0 }} />

        {/* Thumb */}
        <div className="w-10 h-10 rounded-xl flex items-center justify-center shrink-0"
          style={{ background: active ? `linear-gradient(135deg,rgba(255,204,0,0.18),rgba(91,45,142,0.28))` : "#1c1c1c" }}>
          {active && isPlaying
            ? <SpectrumBars color={C.active} />
            : <Music2 size={16} color={active ? C.active : C.dormant} opacity={active ? 1 : 0.4} />}
        </div>

        {/* Text */}
        <div className="flex-1 min-w-0">
          <p className="text-sm font-semibold truncate" style={{ color: active ? C.active : "#bbb" }}>{track.title}</p>
          <p className="text-xs mt-0.5" style={{ color: C.subtext }}>{track.artist}</p>
        </div>
      </div>
    </div>
  );
}

// ── Track list ────────────────────────────────────────────────────────────────
function TrackList({ tracks, activeId, isPlaying, onSelect }: {
  tracks: typeof TRACKS; activeId: number; isPlaying: boolean; onSelect: (t: typeof TRACKS[0]) => void;
}) {
  return (
    <div className="absolute inset-0 overflow-y-auto" style={{ paddingBottom: 200 }}>
      <div className="sticky top-0 z-10 px-5 pt-12 pb-3" style={{ background:"rgba(13,13,13,0.92)", backdropFilter:"blur(12px)" }}>
        <h1 className="text-2xl font-bold text-white" style={{ fontFamily:"'Space Grotesk',sans-serif" }}>My Music</h1>
        <p className="text-xs mt-0.5" style={{ color: C.subtext }}>{tracks.length} tracks · swipe left for options</p>
      </div>
      <div className="px-3 mt-2">
        {tracks.map((track) => (
          <TrackRow
            key={track.id}
            track={track}
            active={track.id === activeId}
            isPlaying={isPlaying}
            onSelect={() => onSelect(track)}
          />
        ))}
      </div>
    </div>
  );
}

// ── Root ──────────────────────────────────────────────────────────────────────
export default function App() {
  const [currentTrack, setCurrentTrack] = useState(TRACKS[0]);
  const [isPlaying, setIsPlaying] = useState(true);
  const [isShuffle, setIsShuffle] = useState(false);
  const [isRepeat, setIsRepeat] = useState(false);
  const [showNowPlaying, setShowNowPlaying] = useState(false);

  const handleNext = () => {
    const idx = TRACKS.findIndex((t) => t.id === currentTrack.id);
    setCurrentTrack(TRACKS[(idx + 1) % TRACKS.length]);
  };
  const handlePrev = () => {
    const idx = TRACKS.findIndex((t) => t.id === currentTrack.id);
    setCurrentTrack(TRACKS[(idx - 1 + TRACKS.length) % TRACKS.length]);
  };

  return (
    <div className="relative w-full h-full overflow-hidden" style={{ background: C.bg, fontFamily:"'Inter',sans-serif", maxWidth: 430, margin:"0 auto" }}>
      <TrackList
        tracks={TRACKS}
        activeId={currentTrack.id}
        isPlaying={isPlaying}
        onSelect={(t) => { setCurrentTrack(t); setIsPlaying(true); }}
      />

      {!showNowPlaying && (
        <MiniPlayerBar
          track={currentTrack}
          isPlaying={isPlaying}
          isShuffle={isShuffle}
          isRepeat={isRepeat}
          onPlayPause={() => setIsPlaying(!isPlaying)}
          onPrev={handlePrev}
          onNext={handleNext}
          onShuffle={() => setIsShuffle(!isShuffle)}
          onRepeat={() => setIsRepeat(!isRepeat)}
          onOpen={() => setShowNowPlaying(true)}
        />
      )}

      {showNowPlaying && (
        <NowPlayingScreen
          track={currentTrack}
          isPlaying={isPlaying}
          isShuffle={isShuffle}
          isRepeat={isRepeat}
          onClose={() => setShowNowPlaying(false)}
          onPlayPause={() => setIsPlaying(!isPlaying)}
          onShuffle={() => setIsShuffle(!isShuffle)}
          onRepeat={() => setIsRepeat(!isRepeat)}
          onPrev={handlePrev}
          onNext={handleNext}
        />
      )}
    </div>
  );
}
