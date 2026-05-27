# Copy-button and persistent scheduled list - code snippets

There is no frontend repo here, so below are ready-to-paste snippets for React, Vue and plain JS that implement:
- show a copy button next to provider message id
- copy to clipboard when button clicked (no manual selection)
- persist (re-fetch) scheduled list whenever user navigates back to the Scheduled page

1) React (functional component)

```jsx
// ScheduledItem.jsx
import React from 'react';

function CopyButton({ text }) {
  const copy = async () => {
    if (!text) return;
    try {
      await navigator.clipboard.writeText(text);
      // optionally show toast
      console.log('Copied:', text);
    } catch (e) {
      console.error('Copy failed', e);
    }
  };
  return (
    <button onClick={copy} aria-label="Copy ID">📋</button>
  );
}

export default function ScheduledItem({ item }) {
  return (
    <div className="scheduled-item">
      <div>ID: {item.id} <CopyButton text={item.providerMessageId || item.id} /></div>
      <div>Recipient: {item.recipient}</div>
      <div>Text: {item.text}</div>
      <div>Status: {item.status}</div>
    </div>
  );
}
```

To keep list persistent, make the scheduled list component fetch on mount/when route activates, and store in global state (context, redux, or localStorage). Example with useEffect:

```jsx
useEffect(() => {
  fetch('/api/v1/scheduled-notifications/pending')
    .then(res => res.json())
    .then(setList);
}, []); // if your router remounts component on navigate back, this ensures refresh
```

2) Plain JS (no framework)

```html
<button id="copyBtn">Copy</button>
<script>
const text = 'provider-id-or-id';
document.getElementById('copyBtn').addEventListener('click', async () => {
  try {
    await navigator.clipboard.writeText(text);
    alert('Copied');
  } catch (e) { alert('Copy failed'); }
});
</script>
```

3) Vue 3 (composition API)

```vue
<template>
  <div>
    <span>{{ item.id }}</span>
    <button @click="copy(item.providerMessageId || item.id)">📋</button>
  </div>
</template>

<script setup>
import { ref } from 'vue'
const props = defineProps({ item: Object })
const copy = async (text) => {
  if (!text) return
  try { await navigator.clipboard.writeText(text) } catch(e) { console.error(e) }
}
</script>
```

UX notes:
- Show providerMessageId prominently when available. If not yet available, show notification `ID` and a copy button for that ID.
- When user navigates away and returns, re-fetch `/pending` so the list is always server-canonical (no local-only state). Optionally cache in localStorage for transient offline UX.

If you want, I can add a minimal HTML page under `notification-gateway/src/main/resources/static/` that demonstrates the scheduled list and copy button and uses the real API endpoints — tell me and I will add a tiny demo UI file and wire it to the endpoints.
