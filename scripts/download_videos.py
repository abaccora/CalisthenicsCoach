from pathlib import Path
import shutil
import subprocess
import sys
import gdown

VIDEOS = {
    "bulgarian_split_squat.mp4": "1em_ZK3E5eh7IBBzsseVIle-C2OOZiugm",
    "calf_raise.mp4": "1VaWzj5AX9cFVbuzzE9T-cg_NAITPLEzA",
    "hip_thrust.mp4": "1pRs7jRZT_XDCxjQ4HGjOvh0RIfz46Igs",
    "reverse_hyperextension.mp4": "1EoXJVRzDkXhCbfNShlyKN9KjMgdDemf6",
    "single_leg_deadlift.mp4": "19Q3dLj-FlKRsDEB97Dfhj-l7LySWQ89l",
    "split_squat.mp4": "1XDyPXxQkvlOFeoxXdoYvRtK8o2hc4zkB",
    "sumo_squat.mp4": "108e-IopT-sCnt73CbruYJS7_VaqQXy4T",
    "air_squat.mp4": "1SAgzUNq7m5Rz0EOcq90ghtlmuoq27P_k",
    "chest_dip.mp4": "1y55Ix69YhAo4s6HA8fdR2nuAnj1rNhdD",
    "decline_push_up.mp4": "18ggRdhwiXneDuNoS-fYfAtOCkVg1tCBH",
    "deficit_push_up.mp4": "1XNLWgYc92okJtgv0hvYo9g4Y1kQk_lFq",
    "incline_push_up.mp4": "1AW0OXlHKU2zpP82lBURVvU4V98qn1fGk",
    "regular_push_up.mp4": "1MpptAKwqh5bK4EZi_N-LL_u68id6PuXu",
    "burpee.mp4": "1wrlHn_kExd7QG2ZqygANL73e3izpI5YL",
    "butt_kicks.mp4": "1LIbCIOKz66_4OfEtOI8HzZ34QcBZQMUT",
    "high_knee_taps.mp4": "1kZHGnokTEu-PKSVsQfWDjs7pEUJi3-FL",
    "jump_rope.mp4": "1VCv3CkpJUyaVXLRq9dwpnF_F_tbw5mCo",
    "running_in_place.mp4": "1xSsI36ZNeei1cYgrV9iX7VEmxURCNJTi",
    "running_in_place_punches.mp4": "1HK-Jr5jJ31Ehl48W-PRWe_aX-WZLqgT4",
    "jumping_jack.mp4": "1Z-NY8rAH_IhlH4whutCK4z067Ab7_PuE",
    "alternating_superman.mp4": "1zilc2cwv17h5cV2vRk3kbqD_oR0Hzk96",
    "inverted_row.mp4": "1NXRHzw_hDXqVTLVQYvaOjtai8FxqgpB-",
    "lying_back_extension.mp4": "1zavUKlmMNGclRA1eK6eL_0NPDRWuCCBN",
    "parallel_grip_pull_up.mp4": "1NhHhbEHMO27LgQxrmZhSfRvJeNn5-3cK",
    "regular_pull_up.mp4": "1gTgT5LaiBAawknUgYeeCw8tlGRMlWWLG",
    "bench_dip.mp4": "1eI2vAf2hG5H3Y7cOuyh9XSllRX8RpQYj",
    "body_saw.mp4": "1bXwDS2sdkvL0fByP9MGtR2V8InDmym-y",
    "pike_push_up.mp4": "1mh1EcpzVCMUKR2n2ypOI_z4qD3zYN3AA",
    "plank_shoulder_tap.mp4": "1KPvDVrb1krT6mk4UEZrD8U5aduJHdAwp",
    "pseudo_push_up.mp4": "1AsRkO1MgPPaBUuQ_oFSuqxoGF_BVuWuH",
    "tricep_extension.mp4": "12440iBB8JDaQW3uJL1W_sS8x3qEp4zi-",
    "bicycle_crunch.mp4": "1U5BcSki-ksWDA4pGQNN919CGPLnjWZp2",
    "dead_bug.mp4": "1HIbGsK_4jXcuYznHYa7yXWwgHjjpMzwq",
    "reverse_crunch.mp4": "16DbFKGM6gDmSs-b0DO5_NIvFnr_iQWdU",
    "side_crunch.mp4": "1wwPyadR-0dAyNxCj54jfG07qrAr9j-Bp",
    "sit_up.mp4": "1gexIa-shNlSlUDORoAu3GL3EyXX-pvFb",
    "crunch.mp4": "1wQvHa56UuTuci7dFhbmxG-FX_nqXFOez",
    "hip_flexor_stretch.mp4": "1wZt5Zizk0wgy_8KLKf-dnI2OhA_kRN4a",
    "kneeling_hamstring_stretch.mp4": "1Wl0Kf6Tw-Wv95MkbRxQodMlqP_rVVWhN",
    "lunging_calf_stretch.mp4": "1qdSFzFxWq77t-kB4dhgazaNTg4QPLAsK",
    "lying_glute_stretch.mp4": "1MSph-qKM5KCkWZU4DvJakh5B6s0_SV4U",
    "side_lunge_stretch.mp4": "196UAEzLpVIXjpKog27RPs2mkFPuCO7Fb",
    "standing_forward_bending.mp4": "1j6aurD-_i09Pozy_Do4MpsVuDBEXerhl",
    "standing_knee_to_chest_stretch.mp4": "1OKtCO1aNNcMExlafNHejUM9RTl-LhpF-",
    "standing_quadricep_stretch.mp4": "1I3aX6Hu9GtcXhRgp0RmQoQoYxi3fHa-i",
    "bench_kneeling_lat_stretch.mp4": "1B0GtcQBqYqQEMjIyTd7Z4EEnTXwxmo1d",
    "cat_cow_stretch.mp4": "1tkXuRMeRgAi0brnyDqSU7uECKfdv51jq",
    "child_s_pose_back_stretch.mp4": "11EkTXb71sw8B0IQ7eTStdTXZ7wxiujl_",
    "doorway_chest_stretch.mp4": "1mlUWfX6AzcSx6ARAFGw3PXgYH2bP2x6y",
    "downward_facing_dog.mp4": "15X9udr0oOaOvC40kx5FOGXYHqnWcB7bf",
    "overhead_tricep_stretch.mp4": "1z3fuxi1uz-nLDmLFyv90WsFS4zujND5j",
    "rear_deltoid_stretch.mp4": "1kFL3RiCN6akMnDGpfBMrnOro0ATRMAcP",
    "side_tilt.mp4": "1vzldde84WCkp4XOw1bch4Q6ShxBL1UqQ",
    "upward_facing_dog.mp4": "1vXpv5qzIYUIPtfl1vwU9f-M7jKtbf_Tz",
}

target = Path("app/src/main/res/raw")
target.mkdir(parents=True, exist_ok=True)
ffmpeg = shutil.which("ffmpeg")
if not ffmpeg:
    raise SystemExit("ffmpeg is required to normalize exercise videos")

def normalize_video(path: Path) -> None:
    """Produce a mobile-safe H.264/yuv420p MP4 with exactly one video stream."""
    temp = path.with_suffix(".normalized.mp4")
    command = [
        ffmpeg, "-y", "-hide_banner", "-loglevel", "error",
        "-i", str(path),
        "-map", "0:v:0",
        "-vf", "scale=720:-2",
        "-c:v", "libx264",
        "-preset", "veryfast",
        "-crf", "23",
        "-pix_fmt", "yuv420p",
        "-profile:v", "main",
        "-level", "3.1",
        "-movflags", "+faststart",
        "-an",
        str(temp),
    ]
    subprocess.run(command, check=True)
    if not temp.exists() or temp.stat().st_size < 1024:
        raise RuntimeError(f"Normalized output is invalid: {path.name}")
    temp.replace(path)

failed = []
for i, (name, file_id) in enumerate(VIDEOS.items(), 1):
    out = target / name
    print(f"[{i}/{len(VIDEOS)}] {name}", flush=True)
    try:
        result = gdown.download(id=file_id, output=str(out), quiet=False, fuzzy=True)
        if not result or not out.exists() or out.stat().st_size < 1024:
            failed.append(name)
        else:
            normalize_video(out)
    except Exception as exc:
        print(f"ERROR {name}: {exc}", file=sys.stderr)
        failed.append(name)

if failed:
    raise SystemExit("Failed to download: " + ", ".join(failed))
print(f"Downloaded and normalized {len(VIDEOS)} videos")
