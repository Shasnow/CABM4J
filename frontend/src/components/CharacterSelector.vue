<template>
  <div class="modal" id="characterModal">
    <div class="modal-content">
      <div class="modal-header">
        <h3>选择角色</h3>
        <button @click="closeModal" class="btn close-btn">&times;</button>
      </div>
      <div class="modal-body">
        <div class="character-list" id="characterList">
          <div
              v-for="(character, index) in characters"
              :key="index"
              class="character-card"
              @click="selectCharacter(index)"
          >
            <img
                :src="character.image"
                :alt="character.name"
                class="character-portrait"
            />
            <div class="character-name">{{ character.name }}</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
export default {
  name: 'CharacterSelector',
  props: {
    characters: {
      type: Object,
      required: true,
    }
  },
  methods: {
    closeModal() {
      this.$emit('close')
    },
    selectCharacter(index) {
      this.$emit('character-selected', index)
      this.closeModal()
    }
  }
}
</script>

<style scoped>
.modal {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 1000;
}

.modal-content {
  background-color: white;
  border-radius: 8px;
  width: 80%;
  max-width: 600px;
  max-height: 80vh;
  overflow-y: auto;
}

.modal-header {
  padding: 16px;
  border-bottom: 1px solid #eee;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-body {
  padding: 16px;
}

.character-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 16px;
}

.character-card {
  cursor: pointer;
  text-align: center;
  padding: 8px;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.character-card:hover {
  background-color: #f0f0f0;
}

.character-portrait {
  width: 100%;
  height: auto;
  border-radius: 4px;
  margin-bottom: 8px;
}

.character-name {
  font-size: 14px;
  word-break: break-word;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  padding: 0 8px;
}
</style>