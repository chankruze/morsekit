You are operating under a strict React / React Native code structuring discipline.
Apply every rule below to ALL code you write, review, or refactor in this project.
Never deviate from these conventions without explicit user instruction.

---

# Core Principles

- Keep files small and responsibility-focused.
- Avoid massive screen/component files.
- Prefer composition over deeply nested logic.
- Extract business logic, hooks, utilities, constants, and types into dedicated files.
- Optimize for scalability, readability, and maintainability.
- Minimize unnecessary re-renders.
- Keep screen files extremely clean.

---

# Folder Structure

Every feature / screen / module follows this modular layout:

```
<feature>/
├── components/       # UI building blocks for this feature
├── hooks/            # business logic, API logic, derived state
├── services/         # external service calls not covered by hooks
├── utils/            # pure helper functions
├── constants/        # magic-value-free named constants
├── types/            # shared TypeScript types for this feature
├── layouts/          # layout wrappers
├── sections/         # composable page sections
├── screen.tsx        # orchestrator — minimal logic, delegates to hooks/sections
├── layout.tsx        # optional layout shell
└── index.ts          # ONLY if a public API surface is needed — see export rules
```

Add these sub-folders only when the need is concrete:

`store/` · `helpers/` · `api/` · `adapters/` · `mappers/` · `providers/` · `context/` · `animations/` · `styles/`

---

# File Naming Convention

**Rule: hyphen-separated (kebab-case) for ALL file names, no exceptions.**

| ✅ Correct | ❌ Wrong |
|---|---|
| `use-packages-api.ts` | `usePackagesApi.ts` |
| `package-card.tsx` | `PackageCard.tsx` |
| `package-list-header.tsx` | `packageListHeader.tsx` |
| `calculate-total-price.ts` | `calculateTotalPrice.ts` |
| `package-constants.ts` | `packageConstants.ts` |
| `screen.tsx` | `PackagesScreen.tsx` |
| `layout.tsx` | `PackagesLayout.tsx` |

**React export names inside files remain PascalCase:**

```ts
// file: package-card.tsx
export const PackageCard = () => { ... }

// file: screen.tsx
export const PackagesScreen = () => { ... }
```

---

# Screen Files

A screen file (`screen.tsx`) must:

- Only orchestrate sections, layouts, and hooks.
- Contain minimal logic — no business logic inline.
- Delegate rendering to section/component files.
- Have no inline `renderItem` functions.
- Have no inline styles.
- Have no direct API calls.
- Have no large JSX blocks.

```tsx
// ✅ Correct screen.tsx
export const PackagesScreen = () => {
  const filters = usePackageFilters();
  const { listData, ...pagination } = usePackageList(filters.queryParams);

  return (
    <PackagesLayout>
      <PackageListSection listData={listData} {...pagination} filters={filters} />
      <PackageFiltersSheet {...filters} />
    </PackagesLayout>
  );
};
```

---

# Component Rules

- Single responsibility — one job per component.
- Receive clean, typed props — no hidden side effects.
- Reusable by default; feature-specific components live in `components/`.
- Memoize with `React.memo` when the component is rendered in a list or re-renders frequently.

---

# Hook Rules

Hooks must:

- Encapsulate business logic, API logic, and derived state.
- Return only values and callbacks — no JSX.
- Be named `use-<noun>.ts` or `use-<verb>-<noun>.ts`.

```
hooks/
├── use-package-filters.ts   # filter state + query param derivation
├── use-package-list.ts      # infinite query + client-side filtering
└── use-package-pagination.ts
```

---

# Import Rules

**Always use absolute imports via the `@/` alias. Never use deep relative paths.**

```ts
// ✅
import { PackageCard } from "@/screens/packages/list/components/package-card";
import { usePackageFilters } from "@/screens/packages/list/hooks/use-package-filters";

// ❌
import { PackageCard } from "../../../components/package-card";
```

---

# Export Rules — NO Barrel Exports

**Never create barrel `index.ts` files that re-export other modules.**

```ts
// ❌ Never do this
export { PackageCard } from "./package-card";
export { PackageGrid } from "./package-grid";

// ✅ Always import directly from the source file
import { PackageCard } from "@/screens/packages/list/components/package-card";
```

Rationale:
- Clearer dependency tracing.
- No circular dependency risk.
- Better IDE navigation (Go to Definition lands on the real file).
- Avoids hidden/transitive imports.
- Faster TypeScript compilation.

This rule applies to: shared components, UI libraries, hooks, utilities, services, feature modules — everything.

---

# TypeScript Rules

- Prefer explicit types over inference for public APIs and shared state.
- Extract types shared across more than one file into `types/`.
- Keep prop interfaces co-located with the component unless shared.
- Never use `any`. Use `unknown` + type narrowing when the shape is truly unknown.

```ts
// ✅ Explicit, named types
type PackageCardProps = {
  pkg: PackageSummary;
  onPress: (slug: string) => void;
};

// ❌
const PackageCard = ({ pkg, onPress }: any) => { ... }
```

---

# Styling Rules

- Never create giant `StyleSheet` objects in screen files.
- Extract large or reusable style sets into `styles/` or co-located style files.
- Prefer theme tokens (`theme.colors.*`, `theme.spacing.*`, `typography.*`) over hard-coded values.
- Use `useThemedStyles` for dynamic theme-dependent styles.
- Use `StyleSheet.create` for static styles to benefit from native optimisation.

---

# Performance Rules

- Memoize expensive components with `React.memo`.
- Never create anonymous inline callbacks inside list render functions.
- Use `useCallback` for stable function references passed to children.
- Use `useMemo` for derived values that are expensive or affect render equality.
- Prefer `FlashList` over `FlatList` for large / paginated datasets.

---

# FlashList Rules (v2 only)

This project uses `@shopify/flash-list` **v2 only**. Apply these rules:

- Pass `estimatedItemSize` accurately (v2 still benefits from it for initial layout).
- Extract `renderItem` into a named component or function — never inline.
- Pass all mutable state via `extraData` so `renderItem` deps stay `[]`.
- Use `stickyHeaderIndices` + `stickyHeaderConfig={{ hideRelatedCell: true }}` for sticky headers.
- Memoize list item components with `React.memo`.
- Use `getItemType` for heterogeneous lists.
- Use `overrideItemLayout` to set `span` for full-width items in multi-column grids.
- Do NOT use: `windowSize`, `maxToRenderPerBatch`, `initialNumToRender`, `removeClippedSubviews` (v2 manages this internally).

---

# Code Quality Rules

- Remove all dead code immediately.
- Extract every magic value into a named constant.
- Keep functions pure where possible.
- Avoid duplicated logic — extract to a shared util or hook.
- Prefer readable code over clever abstractions.
- Default to **no comments**. Add a comment only when the *why* is non-obvious (a hidden constraint, a subtle invariant, a workaround). Never describe *what* the code does.

---

# Architecture Checklist

Before marking any feature complete, verify:

- [ ] Screen file is clean — no inline logic, no inline styles, no API calls.
- [ ] Business logic lives in `hooks/`.
- [ ] All file names are kebab-case.
- [ ] All React component export names are PascalCase.
- [ ] No barrel `index.ts` re-exports exist.
- [ ] All imports use `@/` absolute paths.
- [ ] Shared types are in `types/`.
- [ ] Magic values are in `constants/`.
- [ ] No `any` types.
- [ ] List items are memoized.
- [ ] FlashList `renderItem` has empty deps `[]` and reads state via `extraData`.
- [ ] Styles use theme tokens.
- [ ] No dead code.
