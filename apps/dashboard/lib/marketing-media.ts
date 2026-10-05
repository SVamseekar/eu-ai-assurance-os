/**
 * Photo slots on the public site. Each value is a path under `public/` or null.
 * While a slot is null the page renders built-in artwork in its place, so nothing breaks.
 * To add a photo: put the file in `public/marketing/` and set the path here (use licensed images only).
 */
/** `position` is the CSS object-position used when the photo is cropped to its box. */
export type MediaSlot = { src: string | null; alt: string; position?: string };

export const marketingMedia = {
  /** Home hero backdrop: Europe at night from orbit. Wide, dark, 2400×1400 or larger. */
  heroEurope: { src: "/marketing/hero-europe.jpg", alt: "", position: "70% center" },
  /** EU AI Act page: EU flags in front of an EU institution building. Portrait, about 3:4. */
  euFlag: { src: "/marketing/eu-flags.jpg", alt: "European Union flags in front of a glass office building", position: "30% center" },
  /** Use-cases page: an engineer reviewing a CI pipeline. Landscape, about 4:3. */
  engineerAtDesk: { src: "/marketing/engineer-desk.jpg", alt: "Engineer reviewing a release pipeline at night" },
} satisfies Record<string, MediaSlot>;
